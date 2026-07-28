package com.vpgh.dms.model.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public class SummaryFeedbackReq {
    @NotNull
    private UUID summaryId;
    @NotNull
    private Boolean isHelpful;
    private String comment;

    public UUID getSummaryId() {
        return summaryId;
    }

    public void setSummaryId(UUID summaryId) {
        this.summaryId = summaryId;
    }

    public Boolean getIsHelpful() {
        return isHelpful;
    }

    public void setIsHelpful(Boolean isHelpful) {
        this.isHelpful = isHelpful;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
