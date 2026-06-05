package io.quarkiverse.cms.runtime.security;

import java.util.Set;

/** The per-request principal facts RLS predicates are evaluated against. */
public record SecurityContext(
        String currentUserId,
        String currentTenant,
        Set<String> roles,
        boolean bypassRowPolicy
) {}
