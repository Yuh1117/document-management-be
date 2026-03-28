package com.vpgh.dms.controller;

import com.vpgh.dms.model.dto.request.DocumentProcessingStatusReq;
import com.vpgh.dms.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal/documents")
public class InternalDocumentController {

    private final DocumentService documentService;

    public InternalDocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PatchMapping("/{id}/processing-status")
    public ResponseEntity<Void> updateProcessingStatus(@PathVariable Integer id,
            @Valid @RequestBody DocumentProcessingStatusReq body) {
        documentService.updateProcessingStatus(id, body.getProcessingStatus(), body.getOcrQualityScore(),
                body.getProcessingError(), body.getExtractedText());
        return ResponseEntity.noContent().build();
    }
}
