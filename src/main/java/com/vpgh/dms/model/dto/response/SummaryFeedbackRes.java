package com.vpgh.dms.model.dto.response;

import java.time.Instant;

public class SummaryFeedbackRes {
    private Integer id;
    private Integer documentId;
    private Boolean isHelpful;
    private String comment;
    private String modelName;
    private String promptVersion;
    private Instant createdAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Integer documentId) {
        this.documentId = documentId;
    }

    public Boolean getIsHelpful() {
        return isHelpful;
    }

    public void setIsHelpful(Boolean helpful) {
        isHelpful = helpful;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
