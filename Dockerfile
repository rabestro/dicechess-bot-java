# Multi-stage build (using glibc-based Debian image for ONNX Runtime compatibility)
FROM eclipse-temurin:25-jdk AS builder
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*
WORKDIR /build

ARG GITHUB_ACTOR
ARG GITHUB_TOKEN

# Copy POM, sources, and models
COPY pom.xml .
COPY src ./src
COPY models ./models

# Generate Maven settings for GitHub Packages authentication
RUN mkdir -p /root/.m2 && cat << EOF > /root/.m2/settings.xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>github-dicechess-engine</id>
            <username>${GITHUB_ACTOR}</username>
            <password>${GITHUB_TOKEN}</password>
        </server>
    </servers>
</settings>
EOF

RUN mvn clean package -DskipTests -s /root/.m2/settings.xml

# Runtime stage (glibc-based Debian image for ONNX Runtime native lib compatibility)
FROM eclipse-temurin:25-jre
WORKDIR /app

ENV JAVA_OPTS="-Xmx256m --enable-native-access=ALL-UNNAMED -XX:+UseG1GC"
ENV PORT=8080
ENV MODEL_PATH="/app/models/baseline.onnx"

COPY --from=builder /build/target/dicechess-bot-java-*.jar /app/app.jar
COPY --from=builder /build/models /app/models

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
