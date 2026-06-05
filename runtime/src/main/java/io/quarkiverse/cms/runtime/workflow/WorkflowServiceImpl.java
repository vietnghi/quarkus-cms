package io.quarkiverse.cms.runtime.workflow;
import java.time.Instant; import java.util.Map; import java.util.concurrent.ConcurrentHashMap;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import io.quarkiverse.cms.runtime.security.SecurityContext;

@ApplicationScoped
public class WorkflowServiceImpl implements WorkflowService {
    private final Map<String, WorkflowState> store = new ConcurrentHashMap<>();
    @Inject Event<EntryStageChanged> event;
    @Override public WorkflowState current(String ct, String id) { return store.get(ct+":"+id); }
    @Override public WorkflowState transition(String ct, String id, String toStage, SecurityContext ctx) {
        var s = new WorkflowState(id, ct, toStage, null, Instant.now());
        store.put(ct+":"+id, s);
        event.fire(new EntryStageChanged(ct, id, toStage, ctx != null ? ctx.getCurrentUserId() : "system"));
        return s;
    }
    @Override public void assign(String ct, String entryId, String assignee) {}
}
