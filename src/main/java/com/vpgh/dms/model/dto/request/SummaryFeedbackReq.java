package com.vpgh.dms.model.dto.request;

import jakarta.validation.constraints.NotNull;

public class SummaryFeedbackReq {
    @NotNull
    private Integer summaryId;
    @NotNull
    private Boolean isHelpful;
    private String comment;

    public Integer getSummaryId() {
        return summaryId;
    }

    public void setSummaryId(Integer summaryId) {
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
