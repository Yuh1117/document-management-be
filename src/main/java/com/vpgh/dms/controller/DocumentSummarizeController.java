package com.vpgh.dms.controller;

import java.util.Locale;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vpgh.dms.model.dto.processor.ProcessorModelsListResponse;
import com.vpgh.dms.model.dto.processor.ProcessorReloadModelResponse;
import com.vpgh.dms.model.dto.response.SummarizeModelInfoRes;
import com.vpgh.dms.model.dto.response.SummarizeModelsListRes;

import java.util.List;
import com.vpgh.dms.model.dto.response.DocumentSummarizeRes;
import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.DocumentSummary;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.service.DocumentService;
import com.vpgh.dms.service.DocumentShareService;
import com.vpgh.dms.service.DocumentSummarizeService;
import com.vpgh.dms.service.ProcessorModelService;
import com.vpgh.dms.util.SecurityUtil;
import com.vpgh.dms.util.annotation.ApiMessage;
import com.vpgh.dms.util.exception.ForbiddenException;
import com.vpgh.dms.util.exception.NotFoundException;

@RestController
@RequestMapping("/api")
public class DocumentSummarizeController {
    private final DocumentService documentService;
    private final DocumentShareService documentShareService;
    private final ProcessorModelService processorModelService;
    private final DocumentSummarizeService documentSummarizeService;

    public DocumentSummarizeController(DocumentService documentService, DocumentShareService documentShareService,
            ProcessorModelService processorModelService, DocumentSummarizeService documentSummarizeService) {
        this.documentService = documentService;
        this.documentShareService = documentShareService;
        this.processorModelService = processorModelService;
        this.documentSummarizeService = documentSummarizeService;
    }

    @GetMapping(path = "/secure/documents/{id}/summarize")
    @ApiMessage(key = "api.document.summarize", message = "Summarize document")
    public ResponseEntity<DocumentSummarizeRes> summarize(@PathVariable Integer id, Locale locale) {
        Document doc = resolveDocumentForView(id, SecurityUtil.getCurrentUserFromThreadLocal());
        DocumentSummary summary = this.documentSummarizeService.summarizeDocument(doc, locale.getLanguage());

        return ResponseEntity.ok(new DocumentSummarizeRes(summary.getId(), summary.getSummaryText(),
                summary.getModelName(), summary.getPromptVersion()));
    }

    @GetMapping(path = "/admin/documents/summarize/models")
    @ApiMessage(key = "api.models.summarize", message = "List summarize models")
    public ResponseEntity<SummarizeModelsListRes> listSummarizeModels() {
        ProcessorModelsListResponse processorResponse = processorModelService.listModels();
        List<SummarizeModelInfoRes> models = processorResponse.models().stream()
                .map(m -> new SummarizeModelInfoRes(m.version(), m.modelName(), m.isActive(), m.createdAt()))
                .toList();
        return ResponseEntity.ok(new SummarizeModelsListRes(models));
    }

    @PostMapping(path = "/admin/documents/summarize/models/reload")
    @ApiMessage(key = "api.models.summarize.reload", message = "Reload summarize model")
    public ResponseEntity<ProcessorReloadModelResponse> reloadSummarizeModel() {
        ProcessorReloadModelResponse result = processorModelService.reloadModel();
        return ResponseEntity.ok(result);
    }

    private Document resolveDocumentForView(Integer id, User user) {
        Document doc = documentService.getDocumentById(id);
        if (doc == null || Boolean.TRUE.equals(doc.getDeleted())) {
            throw new NotFoundException("error.document.notFoundOrDeleted");
        }
        if (!documentShareService.checkCanView(user, doc)) {
            throw new ForbiddenException("error.forbidden.viewDocument");
        }
        return doc;
    }
}
