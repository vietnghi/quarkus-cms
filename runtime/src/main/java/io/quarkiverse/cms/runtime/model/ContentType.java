package io.quarkiverse.cms.runtime.model;

import java.util.List;

/**
 * A content type definition: either a COLLECTION (many entries) or SINGLE (one entry).
 * This is the in-memory, registry-facing representation parsed from a schema.json file.
 */
public record ContentType(
        String apiName,        // singular, e.g. "article"
        String pluralName,     // e.g. "articles"
        Kind kind,
        boolean draftAndPublish,
        List<FieldDefinition> fields
) {
    public enum Kind { COLLECTION, SINGLE }

    public FieldDefinition field(String name) {
        return fields.stream().filter(f -> f.name().equals(name)).findFirst().orElse(null);
    }
}
