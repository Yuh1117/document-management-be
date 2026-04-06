package com.vpgh.dms.model.dto.request;

import jakarta.validation.constraints.NotNull;

public class SummaryFeedbackReq {
    @NotNull
    private Boolean isHelpful;
    private String comment;

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
