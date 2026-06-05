package io.quarkiverse.cms.runtime.model;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * In-memory registry of all known content types. Populated at startup from
 * schema.json files (via the deployment recorder) and mutated at runtime by the
 * Content-Type Builder. Single source of truth the Document Service reads from.
 */
@ApplicationScoped
public class SchemaRegistry {

    private final Map<String, ContentType> byApiName = new ConcurrentHashMap<>();
    private final Map<String, ContentType> byPlural = new ConcurrentHashMap<>();

    public void register(ContentType type) {
        byApiName.put(type.apiName(), type);
        byPlural.put(type.pluralName(), type);
    }

    public Optional<ContentType> byApiName(String apiName) {
        return Optional.ofNullable(byApiName.get(apiName));
    }

    public Optional<ContentType> byPlural(String plural) {
        return Optional.ofNullable(byPlural.get(plural));
    }

    public Collection<ContentType> all() {
        return byApiName.values();
    }

    public boolean isEmpty() {
        return byApiName.isEmpty();
    }
}
