package com.vpgh.dms.model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class SignedUrlRequest {
    @NotNull(message = "documentId không được để trống")
    private Integer documentId;

    @NotNull(message = "expiredTime không được để trống")
    @Pattern(regexp = "3|5|10", message = "expiredTime chỉ được phép là 3, 5 hoặc 10 phút")
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
