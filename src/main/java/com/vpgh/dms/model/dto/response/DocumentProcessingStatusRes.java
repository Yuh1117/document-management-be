package com.vpgh.dms.model.dto.response;

import java.util.UUID;

public class DocumentProcessingStatusRes {
    private UUID id;
    private String processingStatus;

    public DocumentProcessingStatusRes(UUID id, String processingStatus) {
        this.id = id;
        this.processingStatus = processingStatus;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(String processingStatus) {
        this.processingStatus = processingStatus;
    }
}
