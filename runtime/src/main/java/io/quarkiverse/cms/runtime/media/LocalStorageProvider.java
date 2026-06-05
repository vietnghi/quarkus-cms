package io.quarkiverse.cms.runtime.media;
import java.io.*; import java.nio.file.*; import java.util.UUID;
import jakarta.annotation.PostConstruct; import jakarta.enterprise.context.ApplicationScoped; import org.jboss.logging.Logger;
@ApplicationScoped
public class LocalStorageProvider implements StorageProvider {
    private static final Logger LOG = Logger.getLogger(LocalStorageProvider.class); private Path root;
    @PostConstruct void init() { root = Path.of("./cms-uploads").toAbsolutePath(); try { Files.createDirectories(root); } catch (Exception e) { LOG.error(e); } }
    public String store(String folder, String fn, String ct, InputStream data) {
        try { Path dir = root.resolve(folder != null ? folder : ""); Files.createDirectories(dir);
            String n = fn != null ? fn : UUID.randomUUID().toString(); Path t = dir.resolve(n);
            Files.copy(data, t, StandardCopyOption.REPLACE_EXISTING); return folder != null ? folder + "/" + n : n;
        } catch (Exception e) { throw new RuntimeException("Store failed", e); }
    }
    public InputStream retrieve(String path) { try { return Files.newInputStream(root.resolve(path)); } catch (Exception e) { return null; } }
    public boolean delete(String path) { try { return Files.deleteIfExists(root.resolve(path)); } catch (Exception e) { return false; } }
    public String name() { return "local"; }
}
