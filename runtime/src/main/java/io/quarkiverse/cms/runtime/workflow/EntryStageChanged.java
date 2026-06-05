package io.quarkiverse.cms.runtime.workflow;
public record EntryStageChanged(String contentType, String entryId, String toStage, String byUser) {}
