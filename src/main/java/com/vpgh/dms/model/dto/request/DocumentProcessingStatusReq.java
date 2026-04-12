package com.vpgh.dms.model.dto.request;

import com.vpgh.dms.model.constant.ProcessingStatus;
import jakarta.validation.constraints.NotNull;

public class DocumentProcessingStatusReq {

    @NotNull(message = "{validation.document.processingStatus.notNull}")
    private ProcessingStatus processingStatus;
    private String processingReport;
    private String extractedText;
    private String ocrMetrics;

    public ProcessingStatus getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(ProcessingStatus processingStatus) {
        this.processingStatus = processingStatus;
    }

    public String getProcessingReport() {
        return processingReport;
    }

    public void setProcessingReport(String processingReport) {
        this.processingReport = processingReport;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public String getOcrMetrics() {
        return ocrMetrics;
    }

    public void setOcrMetrics(String ocrMetrics) {
        this.ocrMetrics = ocrMetrics;
    }
}
