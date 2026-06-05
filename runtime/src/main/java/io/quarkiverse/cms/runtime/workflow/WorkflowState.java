package io.quarkiverse.cms.runtime.workflow;

import java.time.Instant;

/** The current workflow position of a single entry. */
public record WorkflowState(
        String entryId,
        String contentType,
        String currentStage,
        String assignee,
        Instant enteredAt
) {}
