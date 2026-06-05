package io.quarkiverse.cms.runtime.rest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.quarkiverse.cms.runtime.document.Document;
import io.quarkiverse.cms.runtime.document.DocumentService;
import io.quarkiverse.cms.runtime.document.Query;
import io.quarkiverse.cms.runtime.model.CmsRelation;
import io.quarkiverse.cms.runtime.model.SchemaRegistry;

@Path("/cms-admin/api")
@Produces(MediaType.APPLICATION_JSON)
public class AdminResource {
    @Inject SchemaRegistry registry;
    @Inject DocumentService documents;

    @GET @Path("/content-types")
    public List<ContentTypeDto> listContentTypes() {
        return registry.all().stream().map(ct -> new ContentTypeDto(ct.apiName(), ct.pluralName(),
                ct.kind().name(), ct.draftAndPublish(), ct.fields().size())).collect(Collectors.toList());
    }
    @GET @Path("/content-types/{apiName}")
    public ContentTypeDto getContentType(@PathParam("apiName") String apiName) {
        var ct = registry.byApiName(apiName).orElseThrow(() -> new NotFoundException());
        return new ContentTypeDto(ct.apiName(), ct.pluralName(), ct.kind().name(), ct.draftAndPublish(), ct.fields().size());
    }
    @GET @Path("/content-types/{plural}/entries")
    public Map<String, Object> listEntries(@PathParam("plural") String plural,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("pageSize") @DefaultValue("25") int pageSize,
            @QueryParam("status") String status) {
        String typeName = registry.byPlural(plural).orElseThrow(() -> new NotFoundException()).apiName();
        var q = new Query(Map.of(), List.of(), page, pageSize, List.of(), List.of(), null, status);
        var r = documents.find(typeName, q);
        return Map.of("data", r.data(), "meta", Map.of("page", r.page(), "pageSize", r.pageSize(), "total", r.total()));
    }
    @GET @Path("/content-types/{plural}/entries/{id}")
    public Map<String, Object> getEntry(@PathParam("plural") String plural, @PathParam("id") String id) {
        String type = registry.byPlural(plural).orElseThrow(() -> new NotFoundException()).apiName();
        Document doc = documents.findOne(type, id, Query.defaults());
        if (doc == null) throw new NotFoundException();
        return Map.of("data", doc);
    }
    @POST @Path("/content-types/{plural}/entries")
    public Response createEntry(@PathParam("plural") String plural, Map<String, Object> body) {
        String type = registry.byPlural(plural).orElseThrow(() -> new NotFoundException()).apiName();
        return Response.status(201).entity(Map.of("data", documents.create(type, body, null))).build();
    }
    @PUT @Path("/content-types/{plural}/entries/{id}")
    public Map<String, Object> updateEntry(@PathParam("plural") String plural,
                                           @PathParam("id") String id, Map<String, Object> body) {
        String type = registry.byPlural(plural).orElseThrow(() -> new NotFoundException()).apiName();
        Document doc = documents.update(type, id, body);
        if (doc == null) throw new NotFoundException();
        return Map.of("data", doc);
    }
    @DELETE @Path("/content-types/{plural}/entries/{id}")
    public Response deleteEntry(@PathParam("plural") String plural, @PathParam("id") String id) {
        String type = registry.byPlural(plural).orElseThrow(() -> new NotFoundException()).apiName();
        documents.delete(type, id);
        return Response.noContent().build();
    }
    @POST @Path("/relations")
    @jakarta.transaction.Transactional
    public Response createRelation(Map<String, String> rel) {
        var r = new CmsRelation();
        r.sourceContentType = rel.getOrDefault("sourceContentType", "article");
        r.sourceEntryId = rel.get("sourceEntryId");
        r.targetContentType = rel.getOrDefault("targetContentType", "article");
        r.targetEntryId = rel.get("targetEntryId");
        r.fieldName = rel.getOrDefault("fieldName", "related");
        r.relationKind = rel.getOrDefault("relationKind", "ONE_TO_ONE");
        r.persist();
        return Response.status(201).entity(Map.of("id", r.id)).build();
    }
    @GET @Path("/relations") public List<CmsRelation> listRelations() { return CmsRelation.listAll(); }
    public record ContentTypeDto(String apiName, String pluralName, String kind, boolean dap, int fieldCount) {}
}
