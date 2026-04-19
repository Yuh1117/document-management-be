package com.vpgh.dms.controller;

import com.vpgh.dms.model.dto.request.ShareReq;
import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.DocumentShare;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.service.DocumentService;
import com.vpgh.dms.service.DocumentShareService;
import com.vpgh.dms.service.UserService;
import com.vpgh.dms.util.SecurityUtil;
import com.vpgh.dms.util.annotation.ApiMessage;
import com.vpgh.dms.util.exception.ForbiddenException;
import com.vpgh.dms.util.exception.NotFoundException;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DocumentShareController {

    private final DocumentShareService documentShareService;
    private final DocumentService documentService;
    private final UserService userService;

    public DocumentShareController(DocumentShareService documentShareService, DocumentService documentService,
            UserService userService) {
        this.documentShareService = documentShareService;
        this.documentService = documentService;
        this.userService = userService;
    }

    @PostMapping(path = "/secure/documents/share")
    @ApiMessage(key = "api.documentShare.create", message = "Create share")
    public ResponseEntity<List<DocumentShare>> share(@Valid @RequestBody ShareReq shareReq) throws MessagingException {
        Document doc = resolveDocumentForEdit(shareReq.getDocumentId(), SecurityUtil.getCurrentUserFromThreadLocal());
        List<DocumentShare> res = this.documentShareService.shareDocument(doc, shareReq.getShares());
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping(path = "/secure/documents/share/{id}")
    @ApiMessage(key = "api.documentShare.detail", message = "View share details")
    public ResponseEntity<List<DocumentShare>> getShare(@PathVariable("id") Integer id) {
        Document doc = resolveDocumentForView(id, SecurityUtil.getCurrentUserFromThreadLocal());
        return ResponseEntity.ok(this.documentShareService.getShares(doc));
    }

    @DeleteMapping("/secure/documents/share/{id}")
    @ApiMessage(key = "api.documentShare.delete", message = "Remove share permission")
    public ResponseEntity<Void> unshare(@PathVariable Integer id, @RequestBody List<Integer> request) {
        Document doc = resolveDocumentForEdit(id, SecurityUtil.getCurrentUserFromThreadLocal());

        List<User> users = this.userService.getAllByIds(request);
        if (users.isEmpty()) {
            throw new NotFoundException("error.user.notFoundWithDot");
        }

        this.documentShareService.removeShares(doc, users);
        return ResponseEntity.noContent().build();
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

    private Document resolveDocumentForEdit(Integer id, User user) {
        Document doc = documentService.getDocumentById(id);
        if (doc == null || Boolean.TRUE.equals(doc.getDeleted())) {
            throw new NotFoundException("error.document.notFoundOrDeleted");
        }
        if (!documentShareService.checkCanEdit(user, doc)) {
            throw new ForbiddenException("error.forbidden.editDocument");
        }
        return doc;
    }

}