package com.vpgh.dms.model.dto.request;

import com.vpgh.dms.model.constant.ProcessingStatus;
import jakarta.validation.constraints.NotNull;

public class DocumentProcessingStatusReq {

    @NotNull(message = "Processing status không được để trống")
    private ProcessingStatus processingStatus;
    private Integer ocrQualityScore;
    private String processingError;

    public ProcessingStatus getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(ProcessingStatus processingStatus) {
        this.processingStatus = processingStatus;
    }

    public Integer getOcrQualityScore() {
        return ocrQualityScore;
    }

    public void setOcrQualityScore(Integer ocrQualityScore) {
        this.ocrQualityScore = ocrQualityScore;
    }

    public String getProcessingError() {
        return processingError;
    }

    public void setProcessingError(String processingError) {
        this.processingError = processingError;
    }
}
