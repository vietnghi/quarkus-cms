package io.quarkiverse.cms.runtime.rest;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.parameters.RequestBody;
import org.eclipse.microprofile.openapi.models.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.responses.APIResponses;

import io.quarkiverse.cms.runtime.model.ContentType;
import io.quarkiverse.cms.runtime.model.FieldDefinition;
import io.quarkiverse.cms.runtime.model.SchemaRegistry;
import io.quarkiverse.cms.runtime.model.FieldType;
import io.quarkus.arc.Arc;

/**
 * OASFilter that adds per-content-type OpenAPI paths.
 * NOT a CDI bean — instantiated by SmallRye OpenAPI via the
 * open-api-filter property. Gets SchemaRegistry via Arc.container().
 */
public class PerTypeOpenApiFilter implements OASFilter {

    @Override
    public void filterOpenAPI(OpenAPI openAPI) {
        SchemaRegistry registry = Arc.container().select(SchemaRegistry.class).get();
        Components components = openAPI.getComponents();
        if (components == null) {
            components = OASFactory.createObject(Components.class);
            openAPI.setComponents(components);
        }

        for (ContentType ct : registry.all()) {
            String api = ct.apiName();
            String plural = ct.pluralName();

            Schema typeSchema = OASFactory.createObject(Schema.class);
            typeSchema.addType(Schema.SchemaType.OBJECT);
            typeSchema.setDescription("A " + api + " entry");
            for (FieldDefinition f : ct.fields()) {
                Schema prop = OASFactory.createObject(Schema.class);
                prop.addType(fieldTypeToOpenApi(f.type()));
                typeSchema.addProperty(f.name(), prop);
            }
            components.addSchema(api, typeSchema);

            PathItem listPath = OASFactory.createObject(PathItem.class);
            listPath.setGET(listOperation(plural, api));
            listPath.setPOST(createOperation(api));
            openAPI.getPaths().addPathItem("/api/" + plural, listPath);

            PathItem itemPath = OASFactory.createObject(PathItem.class);
            itemPath.setGET(getOperation(api));
            itemPath.setPUT(updateOperation(api));
            itemPath.setDELETE(deleteOperation(api));
            openAPI.getPaths().addPathItem("/api/" + plural + "/{id}", itemPath);
        }
    }

    private Operation listOperation(String plural, String api) {
        Operation op = OASFactory.createObject(Operation.class);
        op.setSummary("List " + plural);
        op.setOperationId("list" + cap(api));
        op.addParameter(qp("page", Schema.SchemaType.INTEGER));
        op.addParameter(qp("pageSize", Schema.SchemaType.INTEGER));
        op.addParameter(qp("sort", Schema.SchemaType.STRING));
        op.addParameter(qp("filters", Schema.SchemaType.STRING));
        op.addParameter(qp("fields", Schema.SchemaType.STRING));
        op.addParameter(qp("populate", Schema.SchemaType.STRING));
        op.addParameter(qp("status", Schema.SchemaType.STRING));
        op.setResponses(resp200(listSchema(api)));
        return op;
    }

    private Operation createOperation(String api) {
        Operation op = OASFactory.createObject(Operation.class);
        op.setSummary("Create " + api);
        op.setOperationId("create" + cap(api));
        op.setRequestBody(reqBody(api));
        op.setResponses(resp201(itemSchema(api)));
        return op;
    }

    private Operation getOperation(String api) {
        Operation op = OASFactory.createObject(Operation.class);
        op.setSummary("Get " + api + " by ID");
        op.setOperationId("get" + cap(api));
        op.addParameter(idParam());
        op.setResponses(resp200or404(itemSchema(api)));
        return op;
    }

    private Operation updateOperation(String api) {
        Operation op = OASFactory.createObject(Operation.class);
        op.setSummary("Update " + api);
        op.setOperationId("update" + cap(api));
        op.addParameter(idParam());
        op.setRequestBody(reqBody(api));
        op.setResponses(resp200or404(itemSchema(api)));
        return op;
    }

    private Operation deleteOperation(String api) {
        Operation op = OASFactory.createObject(Operation.class);
        op.setSummary("Delete " + api);
        op.setOperationId("delete" + cap(api));
        op.addParameter(idParam());
        APIResponses rs = OASFactory.createObject(APIResponses.class);
        rs.addAPIResponse("204", apiResp("No Content"));
        rs.addAPIResponse("404", apiResp("Not Found"));
        op.setResponses(rs);
        return op;
    }

    private Schema listSchema(String api) {
        Schema s = OASFactory.createObject(Schema.class);
        s.addType(Schema.SchemaType.OBJECT);
        Schema data = OASFactory.createObject(Schema.class);
        data.addType(Schema.SchemaType.ARRAY);
        data.setRef("#/components/schemas/" + api);
        s.addProperty("data", data);
        Schema pag = OASFactory.createObject(Schema.class);
        pag.addType(Schema.SchemaType.OBJECT);
        pag.addProperty("page", intSchema());
        pag.addProperty("pageSize", intSchema());
        pag.addProperty("total", intSchema());
        s.addProperty("meta", pag);
        return s;
    }

    private Schema itemSchema(String api) {
        Schema s = OASFactory.createObject(Schema.class);
        s.addType(Schema.SchemaType.OBJECT);
        Schema d = OASFactory.createObject(Schema.class);
        d.addType(Schema.SchemaType.OBJECT);
        d.addProperty("id", strSchema());
        d.addProperty("contentType", strSchema());
        d.addProperty("status", strSchema());
        Schema dataRef = OASFactory.createObject(Schema.class);
        dataRef.setRef("#/components/schemas/" + api);
        d.addProperty("data", dataRef);
        s.addProperty("data", d);
        s.addProperty("meta", OASFactory.createObject(Schema.class));
        return s;
    }

    private Parameter idParam() {
        Parameter p = OASFactory.createObject(Parameter.class);
        p.setName("id"); p.setIn(Parameter.In.PATH); p.setRequired(true);
        Schema s = OASFactory.createObject(Schema.class);
        s.addType(Schema.SchemaType.STRING);
        p.setSchema(s);
        return p;
    }

    private Parameter qp(String name, Schema.SchemaType type) {
        Parameter p = OASFactory.createObject(Parameter.class);
        p.setName(name); p.setIn(Parameter.In.QUERY);
        Schema s = OASFactory.createObject(Schema.class);
        s.addType(type); p.setSchema(s);
        return p;
    }

    private RequestBody reqBody(String api) {
        RequestBody rb = OASFactory.createObject(RequestBody.class);
        rb.setRequired(true);
        Content c = OASFactory.createObject(Content.class);
        MediaType mt = OASFactory.createObject(MediaType.class);
        Schema ref = OASFactory.createObject(Schema.class);
        ref.setRef("#/components/schemas/" + api);
        mt.setSchema(ref);
        c.addMediaType("application/json", mt);
        rb.setContent(c);
        return rb;
    }

    private APIResponses resp200(Schema s) {
        APIResponses rs = OASFactory.createObject(APIResponses.class);
        rs.addAPIResponse("200", contentResp("OK", s));
        return rs;
    }

    private APIResponses resp201(Schema s) {
        APIResponses rs = OASFactory.createObject(APIResponses.class);
        rs.addAPIResponse("201", contentResp("Created", s));
        return rs;
    }

    private APIResponses resp200or404(Schema s) {
        APIResponses rs = OASFactory.createObject(APIResponses.class);
        rs.addAPIResponse("200", contentResp("OK", s));
        rs.addAPIResponse("404", apiResp("Not Found"));
        return rs;
    }

    private APIResponse contentResp(String desc, Schema schema) {
        APIResponse r = apiResp(desc);
        Content c = OASFactory.createObject(Content.class);
        MediaType mt = OASFactory.createObject(MediaType.class);
        mt.setSchema(schema);
        c.addMediaType("application/json", mt);
        r.setContent(c);
        return r;
    }

    private APIResponse apiResp(String desc) {
        APIResponse r = OASFactory.createObject(APIResponse.class);
        r.setDescription(desc);
        return r;
    }

    private Schema strSchema() {
        Schema s = OASFactory.createObject(Schema.class);
        s.addType(Schema.SchemaType.STRING);
        return s;
    }

    private Schema intSchema() {
        Schema s = OASFactory.createObject(Schema.class);
        s.addType(Schema.SchemaType.INTEGER);
        return s;
    }

    private Schema.SchemaType fieldTypeToOpenApi(FieldType ft) {
        return switch (ft) {
            case NUMBER -> Schema.SchemaType.NUMBER;
            case BOOLEAN -> Schema.SchemaType.BOOLEAN;
            default -> Schema.SchemaType.STRING;
        };
    }

    private String cap(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
