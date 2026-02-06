#!/bin/bash

echo "Starting entrypoint script"

echo "Getting SSL certs"
# Retrieve the secret and store it in a variable
secret=$(aws secretsmanager get-secret-value --secret-id $certSecretID --query SecretString --output text --region us-east-1)
# Extract cert and key using jq
cert=$(echo "$secret" | jq -r '.cert')
key=$(echo "$secret" | jq -r '.key')
password=$(echo "$secret" | jq -r '.password')
mkdir -p /tmp/security
# Save the certificate to certificate.cer
echo "$cert" > /tmp/security/certificate.cer
# Save the private key to private.key
echo "$key" > /tmp/security/private.key
# Save the password to password.txt
echo "$password" > /tmp/security/privkeypass.file
echo "$password" > /tmp/security/p12pass.file
# Optional: Set appropriate permissions for the private key
chmod 600 /tmp/security/private.key
cat /tmp/security/certificate.cer > /tmp/security/import.pem

echo "Getting NIST Chain and Root from AWS"
nistCA=$(aws secretsmanager get-secret-value --secret-id $chainSecretID --query SecretString --output text --region us-east-1)
# Extract chain and root using jq
chain=$(echo "$nistCA" | jq -r '.certificates.chain')
root=$(echo "$nistCA" | jq -r '.certificates.root')
# Save the certificate to NISTIssuingCA03.cer
echo "$chain" > /tmp/security/NISTIssuingCA03.cer
# Save the private key to private.key
echo "$root" > /tmp/security/NISTRoot02.cer
echo "Done getting NIST Chain and Root from AWS"

echo "Getting OKTA Root and cert from AWS"
OKTA=$(aws secretsmanager get-secret-value --secret-id $oktaroot --query SecretString --output text --region us-east-1) 
# Extract chain and root using jq
aws secretsmanager get-secret-value --secret-id $oktaroot --query SecretString --output text --region us-east-1 | jq -r '.chain' > /tmp/security/chain.pem
ls -lrt /tmp/security
aws secretsmanager get-secret-value --secret-id $oktaroot --query SecretString --output text --region us-east-1 | jq -r '.cert' > /tmp/security/cert.pem
aws secretsmanager get-secret-value --secret-id $oktaroot --query SecretString --output text --region us-east-1 | jq -r '.root' > /tmp/security/root.pem
storepass=$(echo "$OKTA" | jq -r '.password')
chmod -R 755 /tmp/security
echo "Done getting OKTA Root and cert from AWS"
echo "Getting dependency Root and intermediate from AWS"
# Extract chain and root using jq
aws secretsmanager get-secret-value --secret-id $dependency --query SecretString --output text --region us-east-1 | jq -r '.intermediate' > /tmp/security/depchain.pem
aws secretsmanager get-secret-value --secret-id $dependency --query SecretString --output text --region us-east-1 | jq -r '.root' > /tmp/security/deproot.pem
ls -lrt /tmp/security
storepass=$(echo "$OKTA" | jq -r '.password')
chmod -R 755 /tmp/security
echo "Done getting dependency Root and intermediate from AWS"

# unpackage the war file for file replacement only if it has not unpack yet
if [ ! -f /usr/local/tomcat/webapps/empbc/WEB-INF/web.xml ]; then
	echo "upacking the war file"
	cd /usr/local/tomcat/webapps/
	unzip -q empbc.war -d /usr/local/tomcat/webapps/empbc
fi

# Updating values to variables in server.xml
if [ ! -f /usr/local/tomcat/conf/server.xml ]; then
    echo "Unable to locate server.xml"
    exit 1
else
    # Updated OpenID credentials
	echo "Updating server.xml with keystore, cert and OpenID values"
	sed -i 's/keystorePass_value/'"$KEYSTOREPASS"'/g' /usr/local/tomcat/conf/server.xml
	#grep keystorePass /usr/local/tomcat/conf/server.xml
	sed -i 's/aadClientId_value/'"$AADCLIENTID"'/g' /usr/local/tomcat/conf/server.xml
	sed -i 's/aadSecret_value/'"$AADSECRET"'/g' /usr/local/tomcat/conf/server.xml
    #use @ instead of / in sed as the value has // in it
	sed -i 's@appRedirectURI_value@'"$appRedirectURI"'@g' /usr/local/tomcat/conf/server.xml
	sed -i 's@appLogoutURI_value@'"$appLogoutURI"'@g' /usr/local/tomcat/conf/server.xml
	sed -i 's/appRoles_value/'"$appRoles"'/g' /usr/local/tomcat/conf/server.xml
    # Import certs in NSSDB
    #certutil -D -n docker-fips.nist.gov -d /usr/local/tomcat/nssdb/
    #certutil -D -n "docker-fips.nist.gov #2" -d /usr/local/tomcat/nssdb/
    certutil -L -d /usr/local/tomcat/nssdb
    echo "Importing Root and Chain into NSSDB"
    certutil -A -n NISTChain -t "CT,C,C" -i /tmp/security/NISTIssuingCA03.cer -d /usr/local/tomcat/nssdb
    certutil -A -n NISTRoot -t "CT,C,C" -i /tmp/security/NISTRoot02.cer -d /usr/local/tomcat/nssdb
    certutil -L -d /usr/local/tomcat/nssdb
    echo "Done importing root and chain"
    echo "Importing login.nist.gov and root for OKTA into NSSDB"
    # Set keystore password and paths to certs
      KEYSTORE_PASSWORD=$storepass
      KEYSTORE_PATH="/tmp/security/login_nist-trust.jks"
      CERT_PATH="/tmp/security/logincert.cer"
      ROOT_CERT_PATH="/tmp/security/OKTARootcert.cer"
      ALIAS_CERT="login"
      ALIAS_ROOT="r10_isrg_root"
      # Create a new keystore with a self-signed certificate (if needed)
      echo "Creating an empty keystore..."
      #keytool -keystore $KEYSTORE_PATH -storepass $KEYSTORE_PASSWORD -genkeypair -dname "CN=Empty Keystore" -keyalg RSA -keysize 2048 -validity 365 -alias "temp" -noprompt
      # Delete the temporary alias (if it's no longer needed)
      #keytool -delete -alias temp -keystore $KEYSTORE_PATH -storepass $KEYSTORE_PASSWORD -noprompt
      # Import PEM OKTA certificates
      echo "Importing PEM OKTA certificates into keystore..."
      keytool -importcert -alias login-cert -file /tmp/security/cert.pem -keystore $KEYSTORE_PATH -storepass $KEYSTORE_PASSWORD -noprompt
      keytool -importcert -alias login-chain -file /tmp/security/chain.pem -keystore $KEYSTORE_PATH -storepass $KEYSTORE_PASSWORD -noprompt
      keytool -importcert -alias login-root -file /tmp/security/root.pem -keystore $KEYSTORE_PATH -storepass $KEYSTORE_PASSWORD -noprompt
      echo "Done Importing OKTA certificates"
    echo "Done Importing login.nist.gov and root for OKTA into NSSDB"
    echo "Importing SSL certs into NSSDB"
    openssl pkcs12 -inkey /tmp/security/private.key -passin file:/tmp/security/privkeypass.file -in /tmp/security/import.pem  -export -out /tmp/security/server.p12 -passout file:/tmp/security/p12pass.file
    chmod -R 444 /tmp/security/server.p12
    pk12util -i /tmp/security/server.p12  -d /usr/local/tomcat/nssdb -h "NSS FIPS 140-2 Certificate DB" -k /tmp/security/privkeypass.file -w /tmp/security/p12pass.file
    echo "Importing PEM dependency certificates into NSSDB..."
    # For certificate-only imports, use certutil directly
    certutil -A -n "DependencyChain" -t "CT,C,C" -i /tmp/security/depchain.pem -d /usr/local/tomcat/nssdb
    certutil -A -n "DependencyRoot" -t "CT,C,C" -i /tmp/security/deproot.pem -d /usr/local/tomcat/nssdb
    echo "Done Importing dependency certificates"

    #chown -R tomcat:tomcat /usr/local/tomcat/security
    #chmod -R 700 /usr/local/tomcat/security
    cp -rp $KEYSTORE_PATH /usr/local/tomcat/lib/login_nist-trust.jks
    chown tomcat:tomcat /usr/local/tomcat/lib/login_nist-trust.jks
    chmod 644 /usr/local/tomcat/lib/login_nist-trust.jks
    #rm -rf /tmp/security
    echo "Completed updating server.xml and NSSDB cert import"
    cd /usr/local/tomcat/

fi

# context.xml section
echo "Updating context.xml with Database values"
# exit if no file found
if [ ! -f /usr/local/tomcat/conf/context.xml ]; then
    echo "Unable to locate context.xml"
    exit 1
fi
# replace varaible if context.xml file found
if grep DB_URL /usr/local/tomcat/conf/context.xml; then
    sed -i 's/DB_URL/'"$DB_URL"'/g' /usr/local/tomcat/conf/context.xml
    sed -i 's/DB_PASSWORD/'"$DB_PASSWORD"'/g' /usr/local/tomcat/conf/context.xml
    #sed -i 's/JNDI_NAME/'"$JNDI_NAME"'/g' /usr/local/tomcat/conf/context.xml
    sed -i 's/DB_USER/'"$DB_USER"'/g' /usr/local/tomcat/conf/context.xml
	#sed -i 's/CONNECTION_PSSWD/'"$CONNECTION_PSSWD"'/g' /usr/local/tomcat/conf/context.xml
fi
echo "Completed updating context.xml"
echo "Start Tomcat"
export NSS_DEFAULT_DB_TYPE=sql
# Configure truststore for outbound connections to the NSSDB, refer:- https://www.enterprisedb.com/blog/edb-tutorial-configure-ssl-edb-jdbc-fips-enabled-server
export JAVA_OPTS="$JAVA_OPTS -Djava.library.path=/usr/local/tomcat/native-jni-lib -Xmx2048m -Xms2048m -Djavax.net.ssl.trustStore=/usr/local/tomcat/nssdb -Djavax.net.ssl.trustStorePassword=$KEYSTOREPASS -Djavax.net.ssl.trustStoreType=PKCS11 -Dorg.apache.catalina.security.SecurityListener.UMASK=`umask` -Djava.protocol.handler.pkgs=org.apache.catalina.webresources -Djava.awt.headless=true  -Djava.net.preferIPv4Stack=true -DENV=$ENV -Djavax.net.debug=ssl"
# adding this to allow empbc start as it was using SHA1 algorithm library
#export JAVA_OPTS="$JAVA_OPTS -Djava.security.egd=file:/dev/urandom -Dowasp.csrfguard.PRNG=DRBG"
#export JAVA_OPTS="$JAVA_OPTS -Djavax.net.ssl.trustStore=/usr/local/tomcat/nssdb -Djavax.net.ssl.trustStoreType=PKCS11 -Djavax.net.ssl.trustStorePassword=xComple12PassWr -Dorg.apache.catalina.security.SecurityListener.UMASK=`umask` -Djava.protocol.handler.pkgs=org.apache.catalina.webresources -Djava.awt.headless=true  -Djava.net.preferIPv4Stack=true -DTARGET_MODE=$ENV -Djavax.net.debug=ssl"
#export JAVA_OPTS="$JAVA_OPTS -Dorg.apache.catalina.security.SecurityListener.UMASK=`umask` -Djava.protocol.handler.pkgs=org.apache.catalina.webresources -Djava.awt.headless=true  -Djava.net.preferIPv4Stack=true -Djavax.net.debug=SSL,handshake -Djava.security.debug=sunpkcs11,pkcs12 "

#/usr/local/tomcat/bin/catalina.sh run -security -Dorg.apache.catalina.STRICT_SERVLET_COMPLIANCE=true -Dorg.apache.catalina.connector.RECYCLE_FACADES=true -Dorg.apache.catalina.connector.CoyoteAdapter.ALLOW_BACKSLASH=false -Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=false

/usr/local/tomcat/bin/catalina.sh run 

#/usr/local/tomcat/bin/catalina.sh run & ((while ! echo exit | nc localhost 8443; do sleep 10; done) && /usr/local/tomcat/conf/certs.sh)

