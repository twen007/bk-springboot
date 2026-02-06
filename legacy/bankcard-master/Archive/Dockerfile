
# --- Stage 1: Node.js Builder (Now using pre-built image) ---
FROM sdocker.nist.gov:5050/mml-lab-office/bankcard/sencha-builder:1.0 AS client_builder

# Copy only the frontend directory
COPY frontend/ ./

# Build the app for production
RUN sencha app build production

# --- Stage 2: build war file using Maven Builder---
FROM maven:3.9.9-eclipse-temurin-17 as war_builder

WORKDIR /backend

# Copy backend code
COPY backend/ ./

# --- Remove existing client build files in /backend/src/main/webapp/app/ ---
RUN rm -rf /backend/src/main/webapp/app/*

# Copy client production build files from client_builder
COPY --from=client_builder /app/build/production/bcp /backend/src/main/webapp/app/

# Build the war file
RUN mvn clean package

   # --- Stage 3: Deploy war to Tomcat with JNDI Configuration ---
FROM tomcat:9.0.102-jdk17-corretto AS tomcat_deployer

# Set the working directory in the Tomcat container
WORKDIR /usr/local/tomcat/webapps

# Remove the default ROOT webapp to avoid conflicts
RUN rm -rf ROOT

# Copy the war file from the war_builder stage to the Tomcat webapps directory
COPY --from=war_builder /backend/target/*.war ./

# --- JNDI Configuration ---

# 1. Create the context.xml directory if it doesn't exist
RUN mkdir -p /usr/local/tomcat/conf/Catalina/localhost

# 2. Create a context.xml file with the JNDI resource
# The user/pass here is for a local oracle db. change it based on your config
RUN echo '<Context> \
    <Resource name="jdbc/bcws" \
              auth="Container" \
              type="javax.sql.DataSource" \
              username="BCPMS_OWNER" \
              password="test1234" \
              driverClassName="oracle.jdbc.OracleDriver" \
              url="jdbc:oracle:thin:@//bankcard-db:1521/ORCLPDB1" \
              maxTotal="100" \
              maxIdle="10" /> \
</Context>' > /usr/local/tomcat/conf/Catalina/localhost/empbc.xml

# 3. Copy the Oracle JDBC driver to Tomcat's lib directory
# Download the driver in the build process
RUN curl -o /usr/local/tomcat/lib/ojdbc8.jar https://repo1.maven.org/maven2/com/oracle/database/jdbc/ojdbc8/21.11.0.0/ojdbc8-21.11.0.0.jar

# Expose the Tomcat port
EXPOSE 8080

# Start Tomcat
CMD ["catalina.sh", "run"]