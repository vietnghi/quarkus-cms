package io.quarkiverse.cms.runtime.workflow;

import java.util.List;

/**
 * A basic, role-gated finite state machine for review workflows (Strapi-style
 * Review Workflows). Stages are ordered; transitions list the legal moves and the
 * role required to perform each. Reaching a terminal stage may trigger publish.
 */
public record WorkflowDefinition(
        String name,
        String contentType,
        List<String> stages,          // e.g. ["Draft","In Review","Ready","Published"]
        List<Transition> transitions
) {
    public record Transition(String from, String to, String requiredRole, boolean publishOnEnter) {}

    public boolean isLegal(String from, String to) {
        return transitions.stream().anyMatch(t -> t.from().equals(from) && t.to().equals(to));
    }
}
