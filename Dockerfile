FROM adoptopenjdk:11-jre-hotspot
COPY ./target/*.jar /app/app.jar
COPY ./test_certs/cert_devtest_keystore.jks /app/test/cert_devtest_keystore.jks
WORKDIR /app
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar ./app.jar" ]
