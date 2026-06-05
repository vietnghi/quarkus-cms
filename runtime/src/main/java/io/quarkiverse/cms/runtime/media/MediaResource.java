package io.quarkiverse.cms.runtime.media;
import java.io.InputStream; import java.nio.file.Files; import java.util.Map;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Path("/cms-admin/api/media")
public class MediaResource {
    @Inject StorageProvider storage;
    @POST @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response upload(@RestForm("file") FileUpload file) {
        if (file == null) return Response.status(400).entity(Map.of("error","No file")).build();
        try (InputStream is = Files.newInputStream(file.uploadedFile())) {
            String path = storage.store("uploads", file.fileName(), file.contentType(), is);
            return Response.ok(Map.of("path", path)).build();
        } catch (Exception e) { return Response.serverError().entity(Map.of("error",e.getMessage())).build(); }
    }
}
