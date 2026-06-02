package com.vpgh.dms.model.dto.response;

public class DocumentProcessingStatusRes {
    private Integer id;
    private String processingStatus;

    public DocumentProcessingStatusRes(Integer id, String processingStatus) {
        this.id = id;
        this.processingStatus = processingStatus;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(String processingStatus) {
        this.processingStatus = processingStatus;
    }
}
