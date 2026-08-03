# dicechess-bot-java

[![CI: Java](https://github.com/rabestro/dicechess-bot-java/actions/workflows/ci.yaml/badge.svg)](https://github.com/rabestro/dicechess-bot-java/actions/workflows/ci.yaml)
[![CD: Publish Bot Image](https://github.com/rabestro/dicechess-bot-java/actions/workflows/deploy.yaml/badge.svg)](https://github.com/rabestro/dicechess-bot-java/actions/workflows/deploy.yaml)

Official **Java 25 (LTS)** baseline house bot and reference starter template for the [Dice Chess](https://dicechess.net) platform.

Built with [`dicechess-bot-runtime`](https://github.com/rabestro/dicechess-bot-runtime), [`dicechess-engine-scala`](https://github.com/rabestro/dicechess-engine-scala) (JVM API), and Microsoft ONNX Runtime for Java.

## Overview

This repository serves two primary roles:
1. **Platform Baseline Bot**: An official house bot that runs a greedy material-based ONNX model (`models/baseline.onnx`) to provide a baseline rating in the Dice Chess Ladder.
2. **Developer Starter Template**: A lightweight reference implementation for developers building custom AI bots for Dice Chess in Java.

## Key Features

- **Java 25 & JDK HttpServer**: Built on modern Java 25 (LTS) with minimal dependencies and zero heavy frameworks (~64 MB RAM footprint).
- **ONNX Model Evaluation**: Evaluates candidate full-turn move paths using ONNX value models (`models/baseline.onnx`) with engine heuristic fallback.
- **Bot Runtime Integration**: Uses `lv.id.jc:dicechess-bot-runtime` for HMAC-SHA256 signature verification, webhook handshakes, and `TurnContext` processing.
- **Engine Rules Integration**: Uses `lv.id.jc:dicechess-engine-scala_3` for strict DFEN parsing and legal turn path generation.

## Architecture

```
                       +------------------------+
                       |   Dice Chess Server    |
                       +-----------+------------+
                                   | (HTTP Webhook)
                                   v
+----------------------------------+-----------------------------------+
|                        dicechess-bot-java                            |
|                                                                      |
|  +-----------------------+           +----------------------------+  |
|  |   WebhookHandler      | --------> |        OnnxStrategy        |  |
|  | (dicechess-bot-runtime)           |   (Turn path selection)    |  |
|  +-----------------------+           +-------------+--------------+  |
|                                                    |                 |
|                                    +---------------+---------------+ |
|                                    v                               v |
|                         +--------------------+   +-----------------+ |
|                         |   TurnGenerator    |   |  OnnxEvaluator  | |
|                         | (Scala 3 Engine)   |   | (ONNX Runtime)  | |
|                         +--------------------+   +-----------------+ |
+----------------------------------------------------------------------+
```

## Environment Variables

| Variable                   | Default                | Description                                                |
|----------------------------|------------------------|------------------------------------------------------------|
| `DICECHESS_WEBHOOK_SECRET` | `""`                   | Per-bot secret token for HMAC-SHA256 webhook verification  |
| `PORT`                     | `8080`                 | HTTP server listening port (Koyeb / Cloud Run / VPS)       |
| `MODEL_PATH`               | `models/baseline.onnx` | Path to the ONNX value model file                          |
| `JAVA_OPTS`                | `-Xmx256m`             | JVM memory and GC settings                                 |

## Quick Start

### Prerequisites
- Java 25 (LTS) & Maven 3.9+ (or [`mise`](https://mise.jdx.dev/))

### 1. Build locally
```bash
mise run check
# or using Maven directly:
mvn clean package -s .m2-settings.xml
```

### 2. Run locally
```bash
export DICECHESS_WEBHOOK_SECRET="your-secret-token"
export MODEL_PATH="models/baseline.onnx"
java -jar target/dicechess-bot-java-0.1.0-SNAPSHOT.jar
```

### 3. Run via Docker Container
```bash
docker build -t dicechess-bot-java .
docker run -p 8080:8080 \
  -e DICECHESS_WEBHOOK_SECRET="your-secret-token" \
  ghcr.io/rabestro/dicechess-bot-java:latest
```

## Creating Custom Strategies

To create a custom bot strategy:
1. Implement the `Strategy` interface in `src/main/java/dicechess/bot/`:
   ```java
   public class MyCustomStrategy implements Strategy {
       @Override
       public List<String> chooseMoves(TurnContext context) {
           // Your move selection logic here
       }
   }
   ```
2. Pass your strategy to `WebhookHandler` in `Main.java`.

## License

AGPL-3.0 / MIT (Upstream code). Model files are proprietary platform artifacts.
