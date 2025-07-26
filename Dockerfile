FROM --platform=linux/amd64 eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl

WORKDIR /app

# Copy the Quarkus fast-jar structure
COPY target/quarkus-app/ ./

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "quarkus-run.jar"]