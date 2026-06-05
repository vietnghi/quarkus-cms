package io.quarkiverse.cms.runtime.webhook;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.logging.Logger;

@ApplicationScoped
public class WebhookService {
    private static final Logger LOG = Logger.getLogger(WebhookService.class);
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public void fire(String url, Object payload) {
        try {
            String json = mapper.writeValueAsString(payload);
            var req = HttpRequest.newBuilder().uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(10)).build();
            client.sendAsync(req, HttpResponse.BodyHandlers.discarding())
                    .exceptionally(e -> { LOG.warnf("Webhook failed: %s", e.getMessage()); return null; });
        } catch (Exception e) { LOG.warnf("Webhook error: %s", e.getMessage()); }
    }
}
