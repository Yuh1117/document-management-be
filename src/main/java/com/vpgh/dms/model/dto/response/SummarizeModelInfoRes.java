package com.vpgh.dms.model.dto.response;

public class SummarizeModelInfoRes {
    private String version;
    private String modelName;
    private Boolean isActive;
    private String createdAt;

    public SummarizeModelInfoRes() {
    }

    public SummarizeModelInfoRes(String version, String modelName, Boolean isActive, String createdAt) {
        this.version = version;
        this.modelName = modelName;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
