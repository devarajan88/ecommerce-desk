FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/credit-card-processor-service-*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
