package io.quarkiverse.cms.runtime.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.quarkiverse.cms.runtime.model.ContentType.Kind;

/**
 * Marks a Java class as a CMS content type. Code-first modeling: these classes
 * (under the configured package) are the single source of truth for the data
 * model, discovered at build time and turned into Panache entities + auto APIs.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ContentType {
    /** Singular API name, e.g. "article". Defaults to the decapitalized class name. */
    String api() default "";

    /** Plural API name, e.g. "articles". Defaults to api()+"s". */
    String plural() default "";

    Kind kind() default Kind.COLLECTION;

    boolean draftAndPublish() default true;
}
