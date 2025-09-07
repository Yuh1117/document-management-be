package com.vpgh.dms.controller;

import com.vpgh.dms.model.dto.response.PaginationResDTO;
import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.DocumentVersion;
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
    private final DocumentVersionService documentVersionService;
    private final DocumentShareService documentShareService;

    public DocumentVersionController(DocumentService documentService, DocumentVersionService documentVersionService,
                                     DocumentShareService documentShareService) {
        this.documentService = documentService;
        this.documentVersionService = documentVersionService;
        this.documentShareService = documentShareService;
    }


    @GetMapping(path = "/secure/documents/{id}/versions")
    @ApiMessage(message = "Xem lịch sử phiên bản tài liệu")
    public ResponseEntity<PaginationResDTO<List<DocumentVersion>>> detail(@PathVariable Integer id, @RequestParam Map<String, String> params) {
        Document doc = this.documentService.getDocumentById(id);
        if (doc == null || Boolean.TRUE.equals(doc.getDeleted())) {
            throw new NotFoundException("Tài liệu không tồn tại hoặc đã bị xóa");
        }

        if (!this.documentShareService.checkCanView(SecurityUtil.getCurrentUserFromThreadLocal(), doc)) {
            throw new ForbiddenException("Bạn không có quyền xem tài liệu này");
        }

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

        Document doc = this.documentService.getDocumentById(documentId);
        if (doc == null || Boolean.TRUE.equals(doc.getDeleted())) {
            throw new NotFoundException("Tài liệu không tồn tại hoặc đã bị xóa");
        }

        if (!this.documentShareService.checkCanView(SecurityUtil.getCurrentUserFromThreadLocal(), doc)) {
            throw new ForbiddenException("Bạn không có quyền tải tài liệu này");
        }

        DocumentVersion version = this.documentVersionService.getVersionById(versionId);
        if (version == null) {
            throw new NotFoundException("Phiên bản tài liệu không tồn tại");
        }

        InputStream inputStream = documentService.downloadFileStream(version.getFilePath());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + URLEncoder.encode(version.getName(), StandardCharsets.UTF_8) + "\"")
                .contentType(MediaType.parseMediaType(version.getMimeType()))
                .body(new InputStreamResource(inputStream));
    }

}
