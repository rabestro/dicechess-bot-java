# Multi-stage build
FROM eclipse-temurin:25-jdk-alpine AS builder
RUN apk add --no-cache maven
WORKDIR /build

ARG GITHUB_ACTOR
ARG GITHUB_TOKEN

# Copy POM, Maven settings, and sources
COPY pom.xml .
COPY .m2-settings.xml /root/.m2/settings.xml
COPY src ./src
COPY models ./models

RUN mvn clean package -DskipTests -s /root/.m2/settings.xml

# Runtime stage
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

ENV JAVA_OPTS="-Xmx256m -XX:+UseG1GC"
ENV PORT=8080
ENV MODEL_PATH="/app/models/baseline.onnx"

COPY --from=builder /build/target/dicechess-bot-java-0.1.0-SNAPSHOT.jar /app/app.jar
COPY --from=builder /build/models /app/models

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
