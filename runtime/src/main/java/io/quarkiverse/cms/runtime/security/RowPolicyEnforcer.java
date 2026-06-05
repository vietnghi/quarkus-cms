package io.quarkiverse.cms.runtime.security;

/**
 * Applies Row-Level Security to data access. Implementations enable the relevant
 * Hibernate filters (tenant + row policies) for the current request from the
 * SecurityContext, so the Document Service never returns or mutates rows the
 * caller is not permitted to touch. A single choke point — no adapter bypasses it.
 */
public interface RowPolicyEnforcer {

    /** Enable tenant + row-policy filters for this request. */
    void apply(SecurityContext ctx);

    /** Disable filters (e.g. for a platform-admin / bypass scope). */
    void clear();
}
