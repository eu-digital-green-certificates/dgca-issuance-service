FROM adoptopenjdk:11-jre-hotspot
COPY ./target/*.jar /app/app.jar
COPY ./certs/test.jks /app/certs/test.jks
COPY ./context/context.json /app/context/context.json
WORKDIR /app
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar ./app.jar" ]
