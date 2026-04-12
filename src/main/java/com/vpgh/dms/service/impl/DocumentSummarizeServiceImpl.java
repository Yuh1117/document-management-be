package com.vpgh.dms.service.impl;

import org.springframework.stereotype.Service;

import com.vpgh.dms.model.dto.processor.ProcessorSummarizeRequest;
import com.vpgh.dms.model.dto.processor.ProcessorSummarizeResponse;
import com.vpgh.dms.model.entity.DocumentSummary;
import com.vpgh.dms.repository.DocumentSummarizeRepository;
import com.vpgh.dms.service.DocumentSummarizeService;
import com.vpgh.dms.service.ProcessorSummarizeService;
import com.vpgh.dms.util.SecurityUtil;

import jakarta.transaction.Transactional;

@Service
public class DocumentSummarizeServiceImpl implements DocumentSummarizeService {
    private final ProcessorSummarizeService processorSummarizeService;
    private final DocumentSummarizeRepository documentSummarizeRepository;

    public DocumentSummarizeServiceImpl(ProcessorSummarizeService processorSummarizeService,
            DocumentSummarizeRepository documentSummarizeRepository) {
        this.processorSummarizeService = processorSummarizeService;
        this.documentSummarizeRepository = documentSummarizeRepository;
    }

    @Override
    @Transactional
    public DocumentSummary summarizeDocument(com.vpgh.dms.model.entity.Document doc, String language) {
        String extractedText = doc.getExtractedText();
        if (extractedText == null || extractedText.isBlank()) {
            throw new IllegalStateException("error.document.noExtractedText");
        }

        ProcessorSummarizeResponse response = processorSummarizeService
                .summarize(new ProcessorSummarizeRequest(extractedText, language));

        DocumentSummary summary = new DocumentSummary();
        summary.setDocument(doc);
        summary.setSummaryText(response.summaryText());
        summary.setModelName(response.modelName());
        summary.setPromptVersion(response.promptVersion());
        summary.setCreatedBy(SecurityUtil.getCurrentUserFromThreadLocal());

        return documentSummarizeRepository.save(summary);
    }
}
