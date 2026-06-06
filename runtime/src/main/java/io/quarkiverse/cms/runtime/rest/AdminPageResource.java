package io.quarkiverse.cms.runtime.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.quarkiverse.cms.runtime.document.DocumentService;
import io.quarkiverse.cms.runtime.document.Query;
import io.quarkiverse.cms.runtime.model.ContentType;
import io.quarkiverse.cms.runtime.model.SchemaRegistry;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

@Path("/cms-admin")
public class AdminPageResource {

    @Inject SchemaRegistry registry;
    @Inject DocumentService documents;
    @Inject @Location("admin/dashboard") Template dashboard;
    @Inject @Location("admin/list") Template list;
    @Inject @Location("admin/table") Template table;
    @Inject @Location("admin/form") Template form;

    private TemplateInstance withTypes(Template template, Map<String, Object> data) {
        Map<String, Object> all = new HashMap<>(data != null ? data : Map.of());
        all.put("types", registry.all());
        return template.data(all);
    }

    @GET @Produces(MediaType.TEXT_HTML)
    public TemplateInstance dashboard() {
        return withTypes(dashboard, null);
    }

    @GET @Path("/content/{plural}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance listPage(@PathParam("plural") String plural,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("25") int size,
            @QueryParam("sort") @DefaultValue("") String sort,
            @QueryParam("filter") @DefaultValue("") String filter) {
        ContentType ct = registry.byPlural(plural).orElse(null);
        Map<String, Object> d = new HashMap<>();
        d.put("plural", plural); d.put("type", ct);
        d.put("page", page); d.put("size", size); d.put("sort", sort); d.put("filter", filter);
        return withTypes(list, d);
    }

    @GET @Path("/content/{plural}/table")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance tableFragment(@PathParam("plural") String plural,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("25") int size,
            @QueryParam("sort") @DefaultValue("") String sort,
            @QueryParam("filter") @DefaultValue("") String filter) {
        String typeName = registry.byPlural(plural).orElseThrow(NotFoundException::new).apiName();
        var result = documents.find(typeName, new Query(Query.parseFilters(filter), Query.parseSort(sort), page, size, List.of(), List.of(), null, null));
        long tot = result.total() / size + (result.total() % size > 0 ? 1 : 0);
        var ct = registry.byPlural(plural).get();
        Map<String, Object> d = new HashMap<>();
        d.put("plural", plural); d.put("type", ct); d.put("page", page); d.put("size", size);
        d.put("sort", sort); d.put("filter", filter);
        d.put("entries", result.data()); d.put("total", result.total()); d.put("totalPages", tot);
        return table.data(d);
    }

    @GET @Path("/content/{plural}/create")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance createForm(@PathParam("plural") String plural) {
        ContentType ct = registry.byPlural(plural).orElseThrow(NotFoundException::new);
        Map<String, Object> d = new HashMap<>();
        d.put("plural", plural); d.put("type", ct); d.put("id", null);
        return withTypes(form, d);
    }

    @GET @Path("/content/{plural}/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance editForm(@PathParam("plural") String plural, @PathParam("id") String id) {
        ContentType ct = registry.byPlural(plural).orElseThrow(NotFoundException::new);
        Map<String, Object> d = new HashMap<>();
        d.put("plural", plural); d.put("type", ct); d.put("id", id);
        return withTypes(form, d);
    }

    @POST @Path("/content/{plural}/save")
    public Response saveCreate(@PathParam("plural") String plural, Map<String, Object> body) {
        String type = registry.byPlural(plural).orElseThrow(NotFoundException::new).apiName();
        documents.create(type, body, null);
        return Response.seeOther(java.net.URI.create("/cms-admin/content/" + plural)).build();
    }

    @POST @Path("/content/{plural}/{id}/save")
    public Response saveUpdate(@PathParam("plural") String plural, @PathParam("id") String id, Map<String, Object> body) {
        String type = registry.byPlural(plural).orElseThrow(NotFoundException::new).apiName();
        documents.update(type, id, body);
        return Response.seeOther(java.net.URI.create("/cms-admin/content/" + plural)).build();
    }

    @DELETE @Path("/content/{plural}/{id}")
    public Response deleteEntry(@PathParam("plural") String plural, @PathParam("id") String id) {
        documents.delete(registry.byPlural(plural).orElseThrow(NotFoundException::new).apiName(), id);
        return Response.ok("Deleted").build();
    }

    @GET @Path("/media") @Produces(MediaType.TEXT_HTML)
    public Response mediaPage() { return Response.ok("<h1>Media Library</h1><a href='/cms-admin'>←Back</a>").type("text/html").build(); }
    @GET @Path("/roles") @Produces(MediaType.TEXT_HTML)
    public Response rolesPage() { return Response.ok("<h1>Roles & Policies</h1><a href='/cms-admin'>←Back</a>").type("text/html").build(); }
}
