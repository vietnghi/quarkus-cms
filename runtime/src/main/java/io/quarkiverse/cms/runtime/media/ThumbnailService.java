package io.quarkiverse.cms.runtime.media;
import java.io.InputStream; import java.util.Map;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ThumbnailService {
    public Map<String, String> generate(String path, String fileName, InputStream data) { return Map.of(); }
}
