FROM ghcr.io/graalvm/jdk:java17-22.0.0
VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]