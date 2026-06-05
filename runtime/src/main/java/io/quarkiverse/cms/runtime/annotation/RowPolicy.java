package io.quarkiverse.cms.runtime.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a Row-Level Security policy on a content type. The {@code expression}
 * is a predicate evaluated per request against the caller's identity and tenant
 * (e.g. {@code "author.id = :currentUserId"}), enforced via Hibernate filters /
 * Panache query augmentation (and optionally Postgres native RLS).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(RowPolicy.List.class)
public @interface RowPolicy {

    enum Action { READ, CREATE, UPDATE, DELETE }

    String name();

    /** Predicate; may reference :currentUserId, :currentTenant, and role checks. */
    String expression();

    Action[] appliesTo() default { Action.READ, Action.UPDATE, Action.DELETE };

    /** Roles this policy constrains. Empty = all non-bypass roles. */
    String[] roles() default {};

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface List {
        RowPolicy[] value();
    }
}
