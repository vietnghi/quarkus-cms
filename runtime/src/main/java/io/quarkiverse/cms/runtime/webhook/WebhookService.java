package io.quarkiverse.cms.runtime.webhook;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@ApplicationScoped
public class WebhookService {
    private static final Logger LOG = Logger.getLogger(WebhookService.class);
    public void fire(String url, Object payload) { LOG.infof("Webhook: %s", url); }
}
