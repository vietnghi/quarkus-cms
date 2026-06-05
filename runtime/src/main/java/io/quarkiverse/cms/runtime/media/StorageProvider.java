package io.quarkiverse.cms.runtime.media; import java.io.InputStream;
public interface StorageProvider { String store(String folder, String fn, String ct, InputStream data); InputStream retrieve(String path); boolean delete(String path); String name(); }
