FROM openjdk:8-jdk-alpine
VOLUME /tmp
ARG JAR_FILE
COPY msap-zuul-server-0.1.0.war app.war
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.war"]