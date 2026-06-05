package io.quarkiverse.cms.runtime.rest;

import java.util.List;
import java.util.Map;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.quarkiverse.cms.runtime.document.Document;
import io.quarkiverse.cms.runtime.document.DocumentService;
import io.quarkiverse.cms.runtime.document.Query;
import io.quarkiverse.cms.runtime.model.SchemaRegistry;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
public class ContentResource {

    @Inject DocumentService documents;
    @Inject SchemaRegistry registry;

    private String resolve(String plural) {
        return registry.byPlural(plural)
                .orElseThrow(() -> new jakarta.ws.rs.NotFoundException("No type: " + plural)).apiName();
    }

    @GET
    @Path("/{plural}")
    public Map<String, Object> list(@PathParam("plural") String plural,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("pageSize") @DefaultValue("25") int pageSize,
            @QueryParam("filters") String filtersJson,
            @QueryParam("sort") String sortParam,
            @QueryParam("fields") String fieldsParam,
            @QueryParam("populate") String populateParam,
            @QueryParam("status") String status) {
        Map<String, List<Query.Filter>> filters = Query.parseFilters(filtersJson);
        List<Query.SortField> sort = Query.parseSort(sortParam);
        List<String> fields = fieldsParam != null && !fieldsParam.isBlank() ? List.of(fieldsParam.split(",")) : List.of();
        List<String> populate = populateParam != null && !populateParam.isBlank() ? List.of(populateParam.split(",")) : List.of();
        String ct = resolve(plural);
        Query q = new Query(filters != null ? filters : Map.of(), sort != null ? sort : List.of(),
                page, pageSize, fields, populate, null, status);
        DocumentService.PagedResult<Document> result = documents.find(ct, q);
        return Map.of("data", result.data(), "meta", Map.of("pagination",
                Map.of("page", result.page(), "pageSize", result.pageSize(), "total", result.total())));
    }

    @GET
    @Path("/{plural}/{id}")
    public Map<String, Object> findOne(@PathParam("plural") String plural, @PathParam("id") String id,
            @QueryParam("fields") String fieldsParam, @QueryParam("populate") String populateParam) {
        List<String> populate = populateParam != null && !populateParam.isBlank() ? List.of(populateParam.split(",")) : List.of();
        Document doc = documents.findOne(resolve(plural), id,
                new Query(Map.of(), List.of(), 1, 1,
                        fieldsParam != null && !fieldsParam.isBlank() ? List.of(fieldsParam.split(",")) : List.of(),
                        populate, null, null));
        if (doc == null) throw new NotFoundException();
        return Map.of("data", doc, "meta", Map.of());
    }

    @POST
    @Path("/{plural}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@PathParam("plural") String plural, Map<String, Object> body) {
        Document doc = documents.create(resolve(plural), body, null);
        return Response.status(Response.Status.CREATED).entity(Map.of("data", doc, "meta", Map.of())).build();
    }

    @PUT
    @Path("/{plural}/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Map<String, Object> update(@PathParam("plural") String plural, @PathParam("id") String id,
                                      Map<String, Object> body) {
        Document doc = documents.update(resolve(plural), id, body);
        if (doc == null) throw new NotFoundException();
        return Map.of("data", doc, "meta", Map.of());
    }

    @DELETE
    @Path("/{plural}/{id}")
    public Response delete(@PathParam("plural") String plural, @PathParam("id") String id) {
        documents.delete(resolve(plural), id);
        return Response.noContent().build();
    }
}
