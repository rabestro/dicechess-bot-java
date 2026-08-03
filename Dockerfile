# Multi-stage build
FROM maven:3.9.9-eclipse-temurin-25-alpine AS builder
WORKDIR /build

# Copy POM and resolve dependencies
COPY pom.xml .
# Copy settings if available or build directly
COPY src ./src
COPY models ./models

RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

ENV JAVA_OPTS="-Xmx256m -XX:+UseG1GC"
ENV PORT=8080
ENV MODEL_PATH="/app/models/baseline_nodice.onnx"

COPY --from=builder /build/target/dicechess-bot-java-0.1.0-SNAPSHOT.jar /app/app.jar
COPY --from=builder /build/models /app/models

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
