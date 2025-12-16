# Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /workspace

COPY pom.xml ./
COPY lattice-core/pom.xml lattice-core/pom.xml
COPY lattice-ingestion/pom.xml lattice-ingestion/pom.xml
COPY lattice-retrieval/pom.xml lattice-retrieval/pom.xml
COPY lattice-agent/pom.xml lattice-agent/pom.xml
COPY lattice-application/pom.xml lattice-application/pom.xml

RUN mvn -pl lattice-application -am dependency:go-offline

COPY . .
RUN mvn -pl lattice-application -am package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /workspace/lattice-application/target/*SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
