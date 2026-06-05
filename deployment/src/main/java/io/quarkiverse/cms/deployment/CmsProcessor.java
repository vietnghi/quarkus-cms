package io.quarkiverse.cms.deployment;

import java.util.ArrayList;
import java.util.List;

import io.quarkiverse.cms.runtime.annotation.ContentType;
import io.quarkiverse.cms.runtime.document.Document;
import io.quarkiverse.cms.runtime.document.PanacheDocumentService;
import io.quarkiverse.cms.runtime.document.Query;
import io.quarkiverse.cms.runtime.model.CmsEntry;
import io.quarkiverse.cms.runtime.model.CmsRelation;
import io.quarkiverse.cms.runtime.model.FieldDefinition;
import io.quarkiverse.cms.runtime.model.FieldType;
import io.quarkiverse.cms.runtime.model.SchemaRegistry;
import io.quarkiverse.cms.runtime.rest.ContentResource;
import io.quarkiverse.cms.runtime.config.CmsRecorder;
import io.quarkiverse.cms.runtime.workflow.WorkflowDefinition;
import io.quarkiverse.cms.runtime.workflow.WorkflowState;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;

public class CmsProcessor {

    private static final String FEATURE = "quarkus-cms";
    private static final DotName CONTENT_TYPE = DotName.createSimple(ContentType.class.getName());

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void discoverContentTypes(CombinedIndexBuildItem index, CmsRecorder recorder) {
        List<io.quarkiverse.cms.runtime.model.ContentType> types = new ArrayList<>();
        for (AnnotationInstance ann : index.getIndex().getAnnotations(CONTENT_TYPE)) {
            ClassInfo clazz = ann.target().asClass();
            String className = clazz.name().toString();
            String api = ann.value("api") != null ? ann.value("api").asString() : className;
            String plural = ann.value("plural") != null ? ann.value("plural").asString() : api + "s";
            AnnotationValue kindVal = ann.value("kind");
            io.quarkiverse.cms.runtime.model.ContentType.Kind kind = kindVal != null
                ? io.quarkiverse.cms.runtime.model.ContentType.Kind.valueOf(kindVal.asEnum())
                : io.quarkiverse.cms.runtime.model.ContentType.Kind.COLLECTION;
            boolean dap = ann.value("draftAndPublish") != null ? ann.value("draftAndPublish").asBoolean() : true;

            // Discover fields from the Java class
            List<FieldDefinition> fields = new ArrayList<>();
            for (FieldInfo fi : clazz.fields()) {
                String fieldType = fi.type().name().toString();
                io.quarkiverse.cms.runtime.model.FieldType ft = inferFieldType(fieldType);
                fields.add(new FieldDefinition(fi.name(), ft, false, false, false, List.of(), null, null));
            }

            System.out.println("[quarkus-cms] registering " + className + " → api=" + api + " plural=" + plural + " kind=" + kind);
            types.add(new io.quarkiverse.cms.runtime.model.ContentType(api, plural, kind, dap, fields));
        }
        recorder.registerContentTypes(types);
    }

    @BuildStep
    AdditionalBeanBuildItem beans() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClasses(
                        SchemaRegistry.class,
                        PanacheDocumentService.class,
                        CmsEntry.class,
                        CmsRelation.class,
                        CmsRecorder.class,
                        ContentResource.class)
                .setUnremovable()
                .build();
    }

    @BuildStep
    ReflectiveClassBuildItem reflection() {
        return ReflectiveClassBuildItem.builder(
                        Document.class,
                        Query.class,
                        FieldDefinition.class,
                        WorkflowDefinition.class,
                        WorkflowState.class)
                .methods()
                .fields()
                .build();
    }

    private FieldType inferFieldType(String javaType) {
        return switch (javaType) {
            case "java.lang.String" -> FieldType.TEXT;
            case "int", "long", "double", "float", "java.lang.Integer",
                 "java.lang.Long", "java.lang.Double", "java.lang.Float" ->
                FieldType.NUMBER;
            case "boolean", "java.lang.Boolean" -> FieldType.BOOLEAN;
            default -> FieldType.STRING;
        };
    }
}
