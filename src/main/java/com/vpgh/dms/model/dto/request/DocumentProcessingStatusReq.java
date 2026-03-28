package com.vpgh.dms.model.dto.request;

import com.vpgh.dms.model.constant.ProcessingStatus;
import jakarta.validation.constraints.NotNull;

public class DocumentProcessingStatusReq {

    @NotNull(message = "{validation.document.processingStatus.notNull}")
    private ProcessingStatus processingStatus;
    private Integer ocrQualityScore;
    private String processingError;
    private String extractedText;

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

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }
}
