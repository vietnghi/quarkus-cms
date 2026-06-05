package io.quarkiverse.cms.runtime.security;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RowPolicyEnforcerImpl implements RowPolicyEnforcer {
    @Override public void apply(SecurityContext ctx) {
        // Programmatic tenant filtering handled by SecuredDocumentService
    }
}
