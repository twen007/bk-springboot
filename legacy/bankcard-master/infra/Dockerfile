#FROM 232828153704.dkr.ecr.us-east-1.amazonaws.com/nistoismasd-middleware:tomcat90102-openjdk17-fips-nssdb-rh94_03202025_v1.0
#FROM 232828153704.dkr.ecr.us-east-1.amazonaws.com/nistoismasd-middleware:tomcat90106_openjdk17_fips_nssdb_RH94_07012025_23e73291
#FROM 232828153704.dkr.ecr.us-east-1.amazonaws.com/nistoismasd-middleware:tomcat90107_openjdk17_fips_nssdb_RH94_07282025_385603b8
FROM 232828153704.dkr.ecr.us-east-1.amazonaws.com/nistoismasd-middleware:tomcat90108_openjdk17_fips_nssdb_RH94_09022025_2b486e33

LABEL maintainer="mwadmin@nist.gov"
LABEL name=empbc-Container-Image
LABEL version="1.1"
LABEL release="2.0"
LABEL architecture="x86"
LABEL vendor="GOVT"
LABEL INSTALL="Install ARIF application on Tomcat 9.0.102 with FIPS 140-2 compliance"

#USER root
USER 0

# most of the work will be done in /usr/local/tomcat
WORKDIR /usr/local/tomcat

ENV CATALINA_HOME /usr/local/tomcat
ENV TOMCAT_NATIVE_LIBDIR ${CATALINA_HOME}/native-jni-lib
ENV JAVA_HOME /usr/lib/jvm/jre-17-openjdk
ENV PATH=${CATALINA_HOME}/bin:${PATH}

COPY ./empbc.war /tmp

COPY ./infra/conf/ /usr/local/tomcat/conf/

#RUN rm -rf /usr/local/tomcat/security

COPY ./infra/lib/* /usr/local/tomcat/lib/

RUN microdnf -y update \
    #&& microdnf install -y python3 \
    #&& microdnf install -y python3-pip \
    #&& pip3 install --upgrade awscli \
	&& microdnf -y install procps net-tools iproute lsof binutils file openssl nss-tools nc jq \
    && unzip /tmp/empbc.war -d /usr/local/tomcat/webapps/empbc \
    && rm -rf /usr/local/tomcat/webapps/ROOT \
    && rm -rf /usr/local/tomcat/webapps.dist \
    && microdnf clean all \
    && chmod +x /usr/local/tomcat/conf/config_start.sh \
    && chown -R tomcat:tomcat /usr/local/tomcat/conf/config_start.sh /usr/local/tomcat/conf/server.xml /usr/local/tomcat/conf/web.xml /usr/local/tomcat/webapps \
    && chown -R tomcat:tomcat /usr/local/tomcat/lib /usr/local/tomcat/webapps /usr/local/tomcat/conf  \
    && chmod 600 /usr/local/tomcat/conf/server.xml \
    && chmod 744 /usr/local/tomcat/webapps/ \
    && df --local -P | awk '{if (NR!=1) print $6}' | xargs -I '{}' find '{}' -xdev -type d \( -perm -0002 -a ! -perm -1000 \) 2>/dev/null | xargs -I '{}' chmod a+t '{}' \
    && microdnf clean all

#USER tomcat
USER 1101

WORKDIR /home/tomcat

EXPOSE 8443

ENTRYPOINT ["/usr/local/tomcat/conf/config_start.sh"]

