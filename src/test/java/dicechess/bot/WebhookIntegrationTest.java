package dicechess.bot;

import com.sun.net.httpserver.HttpServer;
import lv.id.jc.dicechess.runtime.CustomHandlerServer;
import lv.id.jc.dicechess.runtime.WebhookHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WebhookIntegrationTest {

    private HttpServer server;
    private OnnxEvaluator evaluator;

    @BeforeEach
    void setUp() throws IOException {
        evaluator = new OnnxEvaluator(null);
        OnnxStrategy strategy = new OnnxStrategy(evaluator);
        WebhookHandler handler = new WebhookHandler("test-secret", strategy);

        // Bind on ephemeral port 0
        server = CustomHandlerServer.start(0, "/api/webhook", handler);
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
        if (evaluator != null) {
            evaluator.close();
        }
    }

    @Test
    void testWebhookRejectsUnauthenticatedRequest() throws Exception {
        int port = server.getAddress().getPort();
        HttpClient client = HttpClient.newHttpClient();

        // A bare GET without proper HMAC signature headers should be rejected
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/webhook"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode(), "Unauthenticated request should return 400");
    }
}
