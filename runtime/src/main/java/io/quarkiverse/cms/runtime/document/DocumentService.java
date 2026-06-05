package io.quarkiverse.cms.runtime.document;

import java.util.List;

/**
 * The canonical CRUD + query engine over the schema registry. REST and GraphQL
 * adapters are thin layers over this single service (Strapi 5 "Document Service"
 * concept). Implementations handle components, dynamic zones, draft/publish and
 * locales.
 */
public interface DocumentService {

    PagedResult<Document> find(String contentType, Query query);

    Document findOne(String contentType, String id, Query query);

    Document create(String contentType, java.util.Map<String, Object> data, String locale);

    Document update(String contentType, String id, java.util.Map<String, Object> data);

    void delete(String contentType, String id);

    Document publish(String contentType, String id);

    Document unpublish(String contentType, String id);

    record PagedResult<T>(List<T> data, long total, int page, int pageSize) {}
}
