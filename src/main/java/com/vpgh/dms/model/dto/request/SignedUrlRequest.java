package com.vpgh.dms.model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class SignedUrlRequest {
    @NotNull(message = "{validation.signedUrl.documentId.notNull}")
    private Integer documentId;

    @NotNull(message = "{validation.signedUrl.expiredTime.notNull}")
    @Pattern(regexp = "3|5|10", message = "{validation.signedUrl.expiredTime.pattern}")
    private String expiredTime;

    public Integer getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Integer documentId) {
        this.documentId = documentId;
    }

    public String getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(String expiredTime) {
        this.expiredTime = expiredTime;
    }
}
