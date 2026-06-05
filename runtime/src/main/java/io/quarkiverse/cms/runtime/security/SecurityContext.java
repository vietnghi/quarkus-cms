package io.quarkiverse.cms.runtime.security;

import java.util.Set;

public class SecurityContext {
    private final String userId; private final String currentTenant;
    private final Set<String> roles; private final boolean bypassRls;
    public SecurityContext(String userId, String currentTenant, Set<String> roles, boolean bypassRls) {
        this.userId = userId; this.currentTenant = currentTenant; this.roles = roles; this.bypassRls = bypassRls;
    }
    public String getCurrentUserId() { return userId; }
    public String getCurrentTenant() { return currentTenant; }
    public Set<String> getRoles() { return roles; }
    public boolean isBypassRls() { return bypassRls; }
}
