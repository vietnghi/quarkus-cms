package io.quarkiverse.cms.runtime.config;

import io.quarkiverse.cms.runtime.tenancy.TenancyStrategy;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Build- and run-time configuration for the Quarkus CMS extension.
 * Exposed under the {@code quarkus.cms.*} namespace.
 */
@ConfigRoot(phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
@ConfigMapping(prefix = "quarkus.cms")
public interface CmsConfig {

    /** Master switch for the CMS extension. */
    @WithDefault("true")
    boolean enabled();

    /** Base path under which the auto-generated content API is served. */
    @WithDefault("/api")
    String apiBasePath();

    /** Path under which the admin SPA is served. */
    @WithDefault("/cms-admin")
    String adminPath();

    /**
     * Package scanned at build time for @ContentType-annotated Java classes
     * (code-first modeling — the single source of truth for the data model).
     */
    @WithDefault("io.quarkiverse.cms.types")
    String typesPackage();

    /** Multi-tenant deployment settings (nested). */
    Tenancy tenancy();

    /** Row-Level Security settings (nested). */
    Rls rls();

    /** Multi-tenant deployment settings. */
    interface Tenancy {
        /** Enable multi-tenant isolation. Default single-tenant. */
        @WithDefault("false")
        boolean enabled();

        /** Isolation strategy. */
        @WithDefault("DISCRIMINATOR")
        TenancyStrategy strategy();
    }

    /** Row-Level Security settings. */
    interface Rls {
        /** Enforce @RowPolicy / admin row policies on all data access. */
        @WithDefault("true")
        boolean enabled();

        /** Also create PostgreSQL native RLS policies as defense in depth. */
        @WithDefault("false")
        boolean nativePostgres();
    }
}
