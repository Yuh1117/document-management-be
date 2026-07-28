package com.vpgh.dms.model.dto.response;

import java.util.UUID;

public class DocumentSummarizeRes {
    private UUID id;
    private String summaryText;
    private String modelName;
    private String promptVersion;

    public DocumentSummarizeRes() {
    }

    public DocumentSummarizeRes(UUID id, String summaryText, String modelName, String promptVersion) {
        this.id = id;
        this.summaryText = summaryText;
        this.modelName = modelName;
        this.promptVersion = promptVersion;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSummaryText() {
        return summaryText;
    }

    public void setSummaryText(String summaryText) {
        this.summaryText = summaryText;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getPromptVersion() {
        return promptVersion;
    }

    public void setPromptVersion(String promptVersion) {
        this.promptVersion = promptVersion;
    }
}
