FROM java:8
VOLUME /tmp
ARG JAR_FILE
ADD ${JAR_FILE} app.jar
EXPOSE 80
ENTRYPOINT ["java","-Dserver.port=80","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]