package io.quarkiverse.cms.runtime.tenancy;

/** Multi-tenancy isolation strategy (quarkus.cms.tenancy.strategy). */
public enum TenancyStrategy {
    /** Shared schema + tenant_id column; pairs with RLS. Default. */
    DISCRIMINATOR,
    /** One DB schema per tenant. */
    SCHEMA,
    /** One datasource per tenant. */
    DATABASE
}
