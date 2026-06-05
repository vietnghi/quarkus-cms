package io.quarkiverse.cms.runtime.document;

import io.quarkiverse.cms.runtime.security.SecurityContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SecuredDocumentService extends PanacheDocumentService {
    @Inject SecurityContext securityContext;
    @Override public PagedResult<Document> find(String ct, Query q) {
        return super.find(ct, q); // Phase 3: add tenant filtering here
    }
}
