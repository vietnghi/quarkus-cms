package io.quarkiverse.cms.runtime.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a content type as tenant-isolated. Under the DISCRIMINATOR strategy this
 * adds a {@code tenant_id} column and a mandatory tenant predicate to every query;
 * under SCHEMA/DATABASE strategies isolation is handled by the datasource/schema.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TenantScoped {
}
