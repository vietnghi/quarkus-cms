package io.quarkiverse.cms.runtime.rest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.quarkiverse.cms.runtime.config.CmsConfig;
import io.quarkiverse.cms.runtime.model.ContentType;
import io.quarkiverse.cms.runtime.model.FieldDefinition;
import io.quarkiverse.cms.runtime.model.FieldType;
import io.quarkiverse.cms.runtime.model.SchemaRegistry;

@Path("/cms-admin/api/codegen")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CodegenResource {
    @Inject SchemaRegistry registry;
    @Inject CmsConfig config;

    public static class CodegenRequest {
        public String className; public String apiName; public String pluralName; public String kind;
        public List<FieldDef> fields;
    }
    public static class FieldDef {
        public String name; public String type; public boolean required;
        public boolean unique; public boolean localized;
        public List<String> enumValues; public String targetType; public String relationKind;
        public List<FieldDef> componentFields;
    }

    @POST @Path("/content-types")
    public Response generateType(CodegenRequest req) {
        if (req == null || req.apiName == null || req.apiName.isBlank())
            return Response.status(400).entity(Map.of("error", "apiName required")).build();
        String source = generateSource(req);
        String pkg = config.typesPackage();
        String outputDir = findSrcDir(pkg.replace('.', '/'));
        if (outputDir == null)
            return Response.serverError().entity(Map.of("error", "Cannot find src dir")).build();
        try {
            Files.writeString(Paths.get(outputDir, req.className + ".java"), source);
            ContentType.Kind k = "SINGLE".equals(req.kind) ? ContentType.Kind.SINGLE : ContentType.Kind.COLLECTION;
            List<FieldDefinition> fds = req.fields != null
                    ? req.fields.stream().map(f -> new FieldDefinition(f.name, FieldType.valueOf(f.type),
                            f.required, f.unique, f.localized,
                            f.enumValues != null ? f.enumValues : List.of(),
                            f.targetType, f.relationKind))
                            .collect(Collectors.toList())
                    : List.of();
            registry.register(new ContentType(req.apiName,
                    req.pluralName != null ? req.pluralName : req.apiName + "s", k, true, fds));
            return Response.ok(Map.of("status", "created", "file", req.className + ".java")).build();
        } catch (Exception e) {
            return Response.serverError().entity(Map.of("error", e.getMessage())).build();
        }
    }

    private String generateSource(CodegenRequest req) {
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(config.typesPackage()).append(";").append(nl).append(nl);
        sb.append("import io.quarkiverse.cms.runtime.annotation.ContentType;").append(nl).append(nl);
        String plural = req.pluralName != null ? req.pluralName : req.apiName + "s";
        sb.append("@ContentType(api = \"").append(req.apiName).append("\", plural = \"").append(plural).append("\")").append(nl);
        sb.append("public class ").append(req.className).append(" {").append(nl);
        if (req.fields != null) {
            for (FieldDef f : req.fields) {
                String jt = switch (f.type) {
                    case "NUMBER" -> "int"; case "BOOLEAN" -> "boolean";
                    case "DATE", "DATETIME" -> "java.time.Instant";
                    default -> "String";
                };
                sb.append("    public ").append(jt).append(" ").append(f.name).append(";").append(nl);
            }
        }
        sb.append("}").append(nl);
        return sb.toString();
    }

    private String findSrcDir(String pkgPath) {
        String[] dirs = {"integration-tests/src/main/java/" + pkgPath,
                "src/main/java/" + pkgPath, "../integration-tests/src/main/java/" + pkgPath};
        for (String d : dirs) { if (Files.exists(Paths.get(d))) return d; }
        return null;
    }
}
