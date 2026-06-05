package io.quarkiverse.cms.runtime.media;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.logging.Logger;

@ApplicationScoped
public class ThumbnailService {
    private static final Logger LOG = Logger.getLogger(ThumbnailService.class);
    private static final List<ThumbSize> SIZES = List.of(new ThumbSize("small", 150, 150), new ThumbSize("medium", 300, 300));

    @Inject StorageProvider storage;

    public Map<String, String> generate(String originalPath, String fileName, InputStream data) {
        try {
            BufferedImage original = ImageIO.read(data);
            if (original == null) { return Map.of(); }
            java.util.HashMap<String, String> results = new java.util.HashMap<>();
            for (ThumbSize size : SIZES) {
                BufferedImage thumb = resize(original, size.width, size.height);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                String fmt = fileName != null && fileName.toLowerCase().endsWith(".jpg") ? "jpeg" : "png";
                ImageIO.write(thumb, fmt, bos);
                String thumbName = size.name + "_" + (fileName != null ? fileName : "thumb.png");
                String path = storage.store("thumbnails", thumbName, "image/" + fmt,
                        new ByteArrayInputStream(bos.toByteArray()));
                results.put(size.name, path);
            }
            return results;
        } catch (Exception e) { LOG.warnf("Thumbnail failed: %s", e.getMessage()); return Map.of(); }
    }
    private BufferedImage resize(BufferedImage src, int tw, int th) {
        double r = Math.min((double) tw / src.getWidth(), (double) th / src.getHeight());
        int nw = (int) (src.getWidth() * r), nh = (int) (src.getHeight() * r);
        BufferedImage d = new BufferedImage(nw, nh, 1);
        Graphics2D g = d.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, nw, nh, null); g.dispose(); return d;
    }
    record ThumbSize(String name, int width, int height) {}
}
