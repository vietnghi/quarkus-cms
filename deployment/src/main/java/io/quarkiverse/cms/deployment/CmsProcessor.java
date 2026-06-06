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
import io.quarkiverse.cms.runtime.rest.AdminPageResource;
import io.quarkiverse.cms.runtime.rest.AdminResource;
import io.quarkiverse.cms.runtime.rest.CodegenResource;
import io.quarkiverse.cms.runtime.rest.ContentResource;
import io.quarkiverse.cms.runtime.rest.PerTypeOpenApiFilter;
import io.quarkiverse.cms.runtime.graphql.GraphQLAdapter;
import io.quarkiverse.cms.runtime.media.LocalStorageProvider;
import io.quarkiverse.cms.runtime.media.MediaResource;
import io.quarkiverse.cms.runtime.media.ThumbnailService;
import io.quarkiverse.cms.runtime.security.RowPolicyEnforcerImpl;
import io.quarkiverse.cms.runtime.security.SecurityContextProducer;
import io.quarkiverse.cms.runtime.document.SecuredDocumentService;
import io.quarkiverse.cms.runtime.tenancy.DefaultTenantResolver;
import io.quarkiverse.cms.runtime.webhook.WebhookService;
import io.quarkiverse.cms.runtime.workflow.WorkflowServiceImpl;
import io.quarkiverse.cms.runtime.config.CmsRecorder;
import io.quarkiverse.cms.runtime.workflow.WorkflowDefinition;
import io.quarkiverse.cms.runtime.workflow.WorkflowState;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.RunTimeConfigurationDefaultBuildItem;
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
    FeatureBuildItem feature() { return new FeatureBuildItem(FEATURE); }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void discoverContentTypes(CombinedIndexBuildItem index, CmsRecorder recorder) {
        List<io.quarkiverse.cms.runtime.model.ContentType> types = new ArrayList<>();
        for (AnnotationInstance ann : index.getIndex().getAnnotations(CONTENT_TYPE)) {
            ClassInfo clazz = ann.target().asClass();
            String cn = clazz.name().toString();
            String api = ann.value("api") != null ? ann.value("api").asString() : cn;
            String plural = ann.value("plural") != null ? ann.value("plural").asString() : api + "s";
            var kind = ann.value("kind") != null
                ? io.quarkiverse.cms.runtime.model.ContentType.Kind.valueOf(ann.value("kind").asEnum())
                : io.quarkiverse.cms.runtime.model.ContentType.Kind.COLLECTION;
            boolean dap = ann.value("draftAndPublish") != null ? ann.value("draftAndPublish").asBoolean() : true;
            List<FieldDefinition> fields = new ArrayList<>();
            for (FieldInfo fi : clazz.fields())
                fields.add(new FieldDefinition(fi.name(), inferFieldType(fi.type().name().toString()), false, false, false, List.of(), null, null));
            System.out.println("[quarkus-cms] registering " + cn + " → api=" + api + " plural=" + plural);
            types.add(new io.quarkiverse.cms.runtime.model.ContentType(api, plural, kind, dap, fields));
        }
        recorder.registerContentTypes(types);
    }

    @BuildStep
    AdditionalBeanBuildItem beans() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClasses(SchemaRegistry.class, CmsEntry.class, CmsRelation.class, CmsRecorder.class,
                        ContentResource.class, AdminResource.class,
 AdminPageResource.class,CodegenResource.class, GraphQLAdapter.class,
                        LocalStorageProvider.class, MediaResource.class, ThumbnailService.class,
                        RowPolicyEnforcerImpl.class, SecurityContextProducer.class, SecuredDocumentService.class,
                        DefaultTenantResolver.class, WebhookService.class, WorkflowServiceImpl.class)
                .setUnremovable().build();
    }

    @BuildStep
    ReflectiveClassBuildItem reflection() {
        return ReflectiveClassBuildItem.builder(Document.class, Query.class, FieldDefinition.class,
                        WorkflowDefinition.class, WorkflowState.class).methods().fields().build();
    }

    @BuildStep
    void openApiFilterConfig(io.quarkus.deployment.annotations.BuildProducer<RunTimeConfigurationDefaultBuildItem> config) {
        config.produce(new RunTimeConfigurationDefaultBuildItem(
                "mp.openapi.filter", "io.quarkiverse.cms.runtime.rest.PerTypeOpenApiFilter"));
        config.produce(new RunTimeConfigurationDefaultBuildItem(
                "smallrye.openapi.filter", "io.quarkiverse.cms.runtime.rest.PerTypeOpenApiFilter"));
    }

    private FieldType inferFieldType(String jt) {
        return switch (jt) {
            case "java.lang.String" -> FieldType.TEXT;
            case "int","long","double","float","java.lang.Integer","java.lang.Long","java.lang.Double","java.lang.Float" -> FieldType.NUMBER;
            case "boolean","java.lang.Boolean" -> FieldType.BOOLEAN;
            default -> FieldType.STRING;
        };
    }
}
