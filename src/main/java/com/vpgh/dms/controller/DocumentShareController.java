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
    @ApiMessage(message = "Tạo mới chia sẻ")
    public ResponseEntity<List<DocumentShare>> share(@Valid @RequestBody ShareReq shareReq) {
        Document doc = this.documentService.getDocumentById(shareReq.getDocumentId());

        if (!this.documentShareService.checkCanEdit(SecurityUtil.getCurrentUserFromThreadLocal(), doc)) {
            throw new ForbiddenException("Bạn không có quyền chia sẻ tài liệu này");
        }

        List<DocumentShare> res = this.documentShareService.shareDocument(doc, shareReq.getShares());
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping(path = "/secure/documents/share/{id}")
    @ApiMessage(message = "Xem chi tiết chia sẻ")
    public ResponseEntity<List<DocumentShare>> getShare(@PathVariable("id") Integer id) {
        Document doc = documentService.getDocumentById(id);
        if (doc == null || Boolean.TRUE.equals(doc.getDeleted())) {
            throw new NotFoundException("Tài liệu không tồn tại hoặc đã bị xoá.");
        }

        if (!this.documentShareService.checkCanView(SecurityUtil.getCurrentUserFromThreadLocal(), doc)) {
            throw new ForbiddenException("Bạn không có quyền xem.");
        }

        List<DocumentShare> res = this.documentShareService.getShares(doc);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @DeleteMapping("/secure/documents/share/{id}")
    @ApiMessage(message = "Xóa quyền chia sẻ")
    public ResponseEntity<Void> unshare(@PathVariable Integer id, @RequestBody List<Integer> request) {
        Document doc = documentService.getDocumentById(id);
        if (doc == null || Boolean.TRUE.equals(doc.getDeleted())) {
            throw new NotFoundException("Tài liệu không tồn tại hoặc đã bị xóa.");
        }

        if (!documentShareService.checkCanEdit(SecurityUtil.getCurrentUserFromThreadLocal(), doc)) {
            throw new ForbiddenException("Bạn không có quyền chỉnh sửa chia sẻ.");
        }

        List<User> users = this.userService.getAllByIds(request);
        if (users.isEmpty()) {
            throw new NotFoundException("Không tìm thấy người dùng.");
        }

        this.documentShareService.removeShares(doc, users);
        return ResponseEntity.noContent().build();
    }

}