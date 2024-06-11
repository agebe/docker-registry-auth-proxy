FROM tomcat:10.1.24-jdk17-temurin-jammy
COPY *.war /usr/local/tomcat/webapps/ROOT.war
COPY server.* /usr/local/tomcat/conf/
COPY logging.properties /usr/local/tomcat/conf/logging.properties
WORKDIR /usr/local/tomcat
EXPOSE 8080
