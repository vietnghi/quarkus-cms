package io.quarkiverse.cms.runtime.tenancy;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.inject.Inject;

@RequestScoped
public class DefaultTenantResolver implements TenantResolver {
    @Inject HttpHeaders headers;
    public String resolveTenantId() {
        try { String t = headers.getHeaderString("X-Tenant"); return (t != null && !t.isBlank()) ? t : "default"; }
        catch (Exception e) { return "default"; }
    }
}
