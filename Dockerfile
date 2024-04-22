FROM amazoncorretto:22-jdk

EXPOSE 8080

ARG JAR_FILE=target/earthone.jar

COPY ./target/earthone.jar earthone.jar

ENTRYPOINT [ "java", "-jar",  "/earthone.jar"]