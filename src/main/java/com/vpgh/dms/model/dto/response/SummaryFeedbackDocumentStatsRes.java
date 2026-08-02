package com.vpgh.dms.model.dto.response;

import java.util.UUID;

public class SummaryFeedbackDocumentStatsRes {
    private UUID documentId;
    private long helpfulCount;
    private long notHelpfulCount;
    private long totalCount;

    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public long getHelpfulCount() {
        return helpfulCount;
    }

    public void setHelpfulCount(long helpfulCount) {
        this.helpfulCount = helpfulCount;
    }

    public long getNotHelpfulCount() {
        return notHelpfulCount;
    }

    public void setNotHelpfulCount(long notHelpfulCount) {
        this.notHelpfulCount = notHelpfulCount;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }
}
