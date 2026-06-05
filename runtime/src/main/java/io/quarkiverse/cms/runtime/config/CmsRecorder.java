package io.quarkiverse.cms.runtime.config;

import java.util.List;

import io.quarkiverse.cms.runtime.model.ContentType;
import io.quarkiverse.cms.runtime.model.SchemaRegistry;
import io.quarkus.arc.Arc;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class CmsRecorder {

    public void registerContentTypes(List<ContentType> types) {
        SchemaRegistry registry = Arc.container().select(SchemaRegistry.class).get();
        for (ContentType ct : types) {
            registry.register(ct);
        }
    }
}
