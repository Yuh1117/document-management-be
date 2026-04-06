package com.vpgh.dms.model.dto.response;

public class DocumentSummarizeRes {
    private Integer id;
    private String summaryText;
    private String modelName;
    private String promptVersion;

    public DocumentSummarizeRes() {
    }

    public DocumentSummarizeRes(Integer id, String summaryText, String modelName, String promptVersion) {
        this.id = id;
        this.summaryText = summaryText;
        this.modelName = modelName;
        this.promptVersion = promptVersion;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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
