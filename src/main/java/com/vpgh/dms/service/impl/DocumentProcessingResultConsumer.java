package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.constant.ProcessingStatus;
import com.vpgh.dms.service.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DocumentProcessingResultConsumer {

    private static final Logger logger = LoggerFactory.getLogger(DocumentProcessingResultConsumer.class);

    private final DocumentService documentService;

    public DocumentProcessingResultConsumer(DocumentService documentService) {
        this.documentService = documentService;
    }

    @RabbitListener(queues = "${rabbitmq.document.processing-result.queue}")
    public void handleProcessingResult(Map<String, Object> payload) {
        Integer documentId = (Integer) payload.get("documentId");
        String statusRaw = (String) payload.get("processingStatus");

        if (documentId == null || statusRaw == null) {
            logger.warn("Received invalid processing result message: {}", payload);
            return;
        }

        ProcessingStatus status;
        try {
            status = ProcessingStatus.valueOf(statusRaw);
        } catch (IllegalArgumentException e) {
            logger.warn("Unknown processingStatus value '{}' for documentId={}", statusRaw, documentId);
            return;
        }

        String processingReport = (String) payload.get("processingReport");
        String extractedText = (String) payload.get("extractedText");
        String processingMetrics = (String) payload.get("processingMetrics");

        logger.info("Received processing result documentId={} status={}", documentId, status);
        documentService.updateProcessingStatus(documentId, status, processingReport, extractedText, processingMetrics);
    }
}
