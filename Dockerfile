# Stage 1: Build the application
FROM maven:3.9.5-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn package -DskipTests 

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN apk add --no-cache curl

RUN curl -L -o /app/opentelemetry-javaagent.jar \
https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar

COPY --from=build /app/target/novel-vippro-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081
EXPOSE 5005

ENTRYPOINT ["java", \
    "-javaagent:/app/opentelemetry-javaagent.jar", \
    "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005",\
    "-Xms128m", \
    "-Xmx256m", \
    "-jar", \
    "app.jar"]