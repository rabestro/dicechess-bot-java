# dicechess-bot-java

Official **Java 25** house bot for the Dice Chess platform. Built with `dicechess-bot-runtime`, `dicechess-engine-scala`
(JVM API), and ONNX Runtime for Java.

## Features

- **Java 25 & JDK HttpServer**: Extremely low memory footprint (~64–128 MB RAM).
- **ONNX Model Evaluation**: Evaluates candidate turn sequences using ONNX value models (`models/baseline.onnx`).
- **Bot Runtime Integration**: Uses `lv.id.jc:dicechess-bot-runtime` for HMAC signature validation, ownership handshake,
  and `TurnContext` processing.
- **Engine Rules Engine**: Uses `lv.id.jc:dicechess-engine-scala_3` for strict DFEN parsing and legal turn generation.

## Environment Variables

| Variable                   | Default                | Description                                         |
|----------------------------|------------------------|-----------------------------------------------------|
| `DICECHESS_WEBHOOK_SECRET` | —                      | Per-bot secret token for HMAC verification          |
| `PORT`                     | `8080`                 | Port for JDK HttpServer (Cloud Run / Koyeb / local) |
| `MODEL_PATH`               | `models/baseline.onnx` | Path to the ONNX value model                        |
| `ORACLE_SEARCH`            | `oneply`               | Search depth (`oneply` or `expectimax`)             |
| `JAVA_OPTS`                | `-Xmx256m`             | Default JVM heap configuration                      |

## Quick Start

### 1. Build locally

```bash
mise run check
# or using maven directly:
mvn clean package
```

### 2. Run locally

```bash
export DICECHESS_WEBHOOK_SECRET="your-secret"
export MODEL_PATH="models/baseline.onnx"
java -jar target/dicechess-bot-java-0.1.0-SNAPSHOT.jar
```

### 3. Docker Container

```bash
docker build -t dicechess-bot-java .
docker run -p 8080:8080 -e DICECHESS_WEBHOOK_SECRET="your-secret" dicechess-bot-java
```

## License

AGPL-3.0 / MIT (Upstream code). Model files are proprietary artifacts.
