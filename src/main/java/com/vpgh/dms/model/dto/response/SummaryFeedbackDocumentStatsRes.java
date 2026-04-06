package com.vpgh.dms.model.dto.response;

public class SummaryFeedbackDocumentStatsRes {
    private Integer documentId;
    private long helpfulCount;
    private long notHelpfulCount;
    private long totalCount;

    public Integer getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Integer documentId) {
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
