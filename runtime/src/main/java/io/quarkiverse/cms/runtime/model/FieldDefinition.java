package io.quarkiverse.cms.runtime.model;

import java.util.List;

/**
 * Definition of a single field on a content type. Mirrors a Strapi attribute.
 */
public record FieldDefinition(
        String name,
        FieldType type,
        boolean required,
        boolean unique,
        boolean localized,
        List<String> enumValues,
        String targetType,    // for RELATION/COMPONENT: the referenced content type/component
        String relationKind   // oneToOne, oneToMany, manyToOne, manyToMany
) {
    public static FieldDefinition simple(String name, FieldType type) {
        return new FieldDefinition(name, type, false, false, false, List.of(), null, null);
    }
}
