package io.quarkiverse.cms.runtime.security;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import java.util.Set;

@RequestScoped
public class SecurityContextProducer {
    @Inject jakarta.ws.rs.core.HttpHeaders headers;
    @Produces @RequestScoped
    public SecurityContext produce() {
        String tenant = "default"; String userId = "anonymous"; boolean bypass = false;
        try {
            String t = headers.getHeaderString("X-Tenant");
            if (t != null && !t.isBlank()) tenant = t;
            String u = headers.getHeaderString("X-User-Id");
            if (u != null && !u.isBlank()) userId = u;
            String b = headers.getHeaderString("X-Bypass-RLS");
            if ("true".equalsIgnoreCase(b)) bypass = true;
        } catch (Exception ignored) {}
        return new SecurityContext(userId, tenant, Set.of("authenticated"), bypass);
    }
}
