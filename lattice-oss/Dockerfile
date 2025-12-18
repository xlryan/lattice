# Build stage
FROM gradle:8.5-jdk21 AS builder
WORKDIR /workspace

COPY settings.gradle build.gradle gradlew ./
COPY gradle gradle
COPY lattice-core/build.gradle lattice-core/
COPY lattice-ingestion/build.gradle lattice-ingestion/
COPY lattice-retrieval/build.gradle lattice-retrieval/
COPY lattice-agent/build.gradle lattice-agent/
COPY lattice-application/build.gradle lattice-application/

COPY . .
RUN ./gradlew :lattice-application:bootJar --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /workspace/lattice-application/build/libs/*SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
