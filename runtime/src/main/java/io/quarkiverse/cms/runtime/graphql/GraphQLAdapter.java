package io.quarkiverse.cms.runtime.graphql;
import java.util.List; import java.util.stream.Collectors;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import io.quarkiverse.cms.runtime.document.Document;
import io.quarkiverse.cms.runtime.document.DocumentService;
import io.quarkiverse.cms.runtime.model.SchemaRegistry;

@GraphQLApi
public class GraphQLAdapter {
    @Inject DocumentService documents;
    @Inject SchemaRegistry registry;
    @Query("contentTypes") public List<String> contentTypes() {
        return registry.all().stream().map(ct -> ct.apiName()).collect(Collectors.toList());
    }
    @Query("entry") public GraphQLEntry entry(String type, String id) {
        Document doc = documents.findOne(type, id, null);
        return doc != null ? GraphQLEntry.from(doc.id(), doc.contentType(), doc.status(), doc.locale(),
                doc.createdAt(), doc.updatedAt(), doc.data()) : null;
    }
}
