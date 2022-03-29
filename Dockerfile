FROM openjdk:18-jdk-alpine3.14
ARG JAR_FILE=target/*.jar
ARG CONTRAST_AGENT_VERSION=3.10.0.25482
ADD https://repo1.maven.org/maven2/com/contrastsecurity/contrast-agent/$CONTRAST_AGENT_VERSION/contrast-agent-$CONTRAST_AGENT_VERSION.jar contrast.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-javaagent:contrast.jar","-jar","/app.jar"]
