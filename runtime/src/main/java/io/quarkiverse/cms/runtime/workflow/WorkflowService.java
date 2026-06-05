package io.quarkiverse.cms.runtime.workflow;

import io.quarkiverse.cms.runtime.security.SecurityContext;

/**
 * Enforces legal, role-permitted stage transitions and fires an EntryStageChanged
 * CDI event on success (webhooks + notifications subscribe to it). Tenant-scoped
 * and RLS-aware. Kept intentionally basic — swap for Kogito/Flowable later behind
 * this same interface if heavier orchestration is needed.
 */
public interface WorkflowService {

    WorkflowState current(String contentType, String entryId);

    /**
     * Move an entry to {@code toStage}.
     * @throws IllegalStateException if the transition is illegal
     * @throws SecurityException     if the caller lacks the required role
     */
    WorkflowState transition(String contentType, String entryId, String toStage, SecurityContext ctx);

    void assign(String contentType, String entryId, String assignee);
}
