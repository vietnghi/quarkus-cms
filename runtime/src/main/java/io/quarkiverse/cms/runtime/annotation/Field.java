package io.quarkiverse.cms.runtime.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Declares a content-type field. Unannotated properties use sensible defaults. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Field {
    boolean required() default false;
    boolean unique() default false;
    boolean localized() default false;
}
