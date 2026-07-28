package com.vpgh.dms.model.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class SignedUrlRequest {
    @NotNull(message = "{validation.signedUrl.documentId.notNull}")
    private UUID documentId;

    @NotNull(message = "{validation.signedUrl.expiredTime.notNull}")
    @Pattern(regexp = "3|5|10", message = "{validation.signedUrl.expiredTime.pattern}")
    private String expiredTime;

    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public String getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(String expiredTime) {
        this.expiredTime = expiredTime;
    }
}
