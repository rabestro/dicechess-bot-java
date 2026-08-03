package dicechess.bot;

import com.sun.net.httpserver.HttpServer;
import lv.id.jc.dicechess.runtime.CustomHandlerServer;
import lv.id.jc.dicechess.runtime.WebhookHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;

/**
 * Entry point for the Dice Chess Java bot.
 */
public class Main {

    private static final Logger logger = System.getLogger(Main.class.getName());
    private static final String DEFAULT_WEBHOOK_PATH = "/api/webhook";

    public static void main(String[] args) {
        var secret = System.getenv().getOrDefault("DICECHESS_WEBHOOK_SECRET", "");
        if (secret.isEmpty()) {
            logger.log(Level.WARNING, "DICECHESS_WEBHOOK_SECRET is not set — webhook verification handshake may fail");
        }

        String modelPath = System.getenv().getOrDefault("MODEL_PATH", "models/baseline.onnx");
        int port = resolvePort();

        OnnxEvaluator evaluator = new OnnxEvaluator(modelPath);
        OnnxStrategy strategy = new OnnxStrategy(evaluator);

        WebhookHandler handler = new WebhookHandler(secret, strategy);

        HttpServer server;
        try {
            server = CustomHandlerServer.start(port, DEFAULT_WEBHOOK_PATH, handler);
            // Register health check endpoints for Koyeb / Cloud Run / Kubernetes
            server.createContext("/", exchange -> {
                byte[] response = "OK".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, response.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            });
            server.createContext("/health", exchange -> {
                byte[] response = "OK".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, response.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            });
        } catch (IOException e) {
            logger.log(Level.ERROR, "Failed to start HTTP server on port {0}: {1}", port, e.getMessage());
            evaluator.close();
            return;
        }

        logger.log(Level.INFO, "Dice Chess Java Bot initialized and listening on port {0} at path {1}", port, DEFAULT_WEBHOOK_PATH);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.log(Level.INFO, "Shutting down Java Bot server...");
            server.stop(1);
            evaluator.close();
        }));

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static int resolvePort() {
        var portStr = System.getenv("PORT");
        if (portStr != null && !portStr.isBlank()) {
            try {
                return Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                logger.log(Level.WARNING, "Invalid PORT environment variable ''{0}'', falling back to 8080", portStr);
            }
        }
        return 8080;
    }
}
