package com.vpgh.dms.controller;

import com.vpgh.dms.model.dto.response.PaginationResDTO;
import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.DocumentVersion;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.service.DocumentService;
import com.vpgh.dms.service.DocumentShareService;
import com.vpgh.dms.service.DocumentVersionService;
import com.vpgh.dms.util.SecurityUtil;
import com.vpgh.dms.util.annotation.ApiMessage;
import com.vpgh.dms.util.exception.ForbiddenException;
import com.vpgh.dms.util.exception.NotFoundException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DocumentVersionController {
    private final DocumentService documentService;
    private final DocumentShareService documentShareService;
    private final DocumentVersionService documentVersionService;

    public DocumentVersionController(DocumentService documentService, DocumentShareService documentShareService,
            DocumentVersionService documentVersionService) {
        this.documentService = documentService;
        this.documentShareService = documentShareService;
        this.documentVersionService = documentVersionService;
    }

    @GetMapping(path = "/secure/documents/{id}/versions")
    @ApiMessage(key = "api.documentVersion.history", message = "View document version history")
    public ResponseEntity<PaginationResDTO<List<DocumentVersion>>> detail(@PathVariable Integer id,
            @RequestParam Map<String, String> params) {
        Document doc = resolveDocumentForView(id, SecurityUtil.getCurrentUserFromThreadLocal());

        String page = params.get("page");
        if (page == null || page.isEmpty()) {
            params.put("page", "1");
        }

        Page<DocumentVersion> pageVersions = this.documentVersionService.getVersionsByDocument(params, doc);

        PaginationResDTO<List<DocumentVersion>> results = new PaginationResDTO<>();
        results.setResult(pageVersions.getContent());
        results.setCurrentPage(pageVersions.getNumber() + 1);
        results.setTotalPages(pageVersions.getTotalPages());

        return ResponseEntity.status(HttpStatus.OK).body(results);
    }

    @GetMapping(path = "/secure/documents/{documentId}/versions/{versionId}/download")
    public ResponseEntity<InputStreamResource> downloadVersion(@PathVariable Integer documentId,
            @PathVariable Integer versionId) {

        resolveDocumentForView(documentId, SecurityUtil.getCurrentUserFromThreadLocal());

        DocumentVersion version = this.documentVersionService.getVersionById(versionId);
        if (version == null) {
            throw new NotFoundException("error.documentVersion.notFound");
        }

        InputStream inputStream = documentService.downloadFileStream(version.getFilePath());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + URLEncoder.encode(version.getName(), StandardCharsets.UTF_8) + "\"")
                .contentType(MediaType.parseMediaType(version.getMimeType()))
                .body(new InputStreamResource(inputStream));
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
