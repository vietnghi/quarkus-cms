package io.quarkiverse.cms.runtime.tenancy;

/**
 * SPI for resolving the current tenant per request. Implementations resolve from
 * a JWT claim, an X-Tenant header, subdomain, or path prefix. Provide a CDI bean
 * implementing this interface to override the default.
 */
public interface TenantResolver {

    /** @return the tenant id for the current request, or the default tenant. */
    String resolveTenantId();

    /** @return the tenant id used when none is resolved (single-tenant default). */
    default String defaultTenantId() {
        return "default";
    }
}
