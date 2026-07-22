FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/with-coworkers-0.0.1-SNAPSHOT.jar app.jar
RUN mkdir -p /app/logs
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]