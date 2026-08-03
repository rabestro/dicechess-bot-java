package dicechess.bot;

import com.sun.net.httpserver.HttpServer;
import lv.id.jc.dicechess.runtime.CustomHandlerServer;
import lv.id.jc.dicechess.runtime.WebhookHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Entry point for the Dice Chess Java bot.
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String DEFAULT_WEBHOOK_PATH = "/api/webhook";

    static void main() {
        var secret = System.getenv().getOrDefault("DICECHESS_WEBHOOK_SECRET", "");
        if (secret.isEmpty()) {
            logger.warn("DICECHESS_WEBHOOK_SECRET is not set — webhook verification handshake may fail");
        }

        var modelPath = System.getenv().getOrDefault("MODEL_PATH", "models/baseline_nodice.onnx");
        int port = resolvePort();

        OnnxEvaluator evaluator = new OnnxEvaluator(modelPath);
        OnnxStrategy strategy = new OnnxStrategy(evaluator);

        WebhookHandler handler = new WebhookHandler(secret, strategy);

        HttpServer server;
        try {
            server = CustomHandlerServer.start(port, DEFAULT_WEBHOOK_PATH, handler);
        } catch (IOException e) {
            logger.error("Failed to start HTTP server on port {}: {}", port, e.getMessage());
            evaluator.close();
            return;
        }

        logger.info("Dice Chess Java Bot initialized and listening on port {} at path {}", port, DEFAULT_WEBHOOK_PATH);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down Java Bot server...");
            server.stop(1);
            evaluator.close();
        }));

        try {
            Thread.currentThread().join();
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
    }

    private static int resolvePort() {
        var portStr = System.getenv("PORT");
        if (portStr != null && !portStr.isBlank()) {
            try {
                return Integer.parseInt(portStr);
            } catch (NumberFormatException _) {
                logger.warn("Invalid PORT environment variable '{}', falling back to 8080", portStr);
            }
        }
        return 8080;
    }
}
