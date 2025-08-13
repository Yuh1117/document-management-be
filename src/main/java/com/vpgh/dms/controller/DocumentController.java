package com.vpgh.dms.controller;

import com.vpgh.dms.model.dto.request.FileUploadReq;
import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.Folder;
import com.vpgh.dms.service.DocumentPermissionService;
import com.vpgh.dms.service.DocumentService;
import com.vpgh.dms.service.FolderService;
import com.vpgh.dms.util.SecurityUtil;
import com.vpgh.dms.util.annotation.ApiMessage;
import com.vpgh.dms.util.exception.ForbiddenException;
import com.vpgh.dms.util.exception.NotFoundException;
import com.vpgh.dms.util.exception.UniqueConstraintException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class DocumentController {
    @Autowired
    private DocumentService documentService;
    @Autowired
    private FolderService folderService;
    @Autowired
    private DocumentPermissionService documentPermissionService;

    @PostMapping(path = "/secure/documents/upload")
    @ApiMessage(message = "Upload tài liệu")
    public ResponseEntity<Document> upload(@Valid @ModelAttribute FileUploadReq fileUploadReq) throws IOException {
        Folder folder = null;
        if (fileUploadReq.getFolderId() != null) {
            folder = this.folderService.getFolderById(fileUploadReq.getFolderId());
            if (documentService.existsByNameAndFolderAndIsDeletedFalseAndIdNot(fileUploadReq.getFile().getOriginalFilename(),
                    folder, null)) {
                throw new UniqueConstraintException("Tài liệu trùng tên trong cùng thư mục.");
            }
        } else {
            if (documentService.existsByNameAndCreatedByAndFolderIsNullAndIsDeletedFalseAndIdNot(fileUploadReq.getFile().getOriginalFilename(),
                    SecurityUtil.getCurrentUserFromThreadLocal(), null)) {
                throw new UniqueConstraintException("Tài liệu trùng tên trong thư mục gốc.");
            }
        }

        Document doc = this.documentService.uploadNewFile(fileUploadReq.getFile(), folder);
        return ResponseEntity.status(HttpStatus.CREATED).body(doc);
    }

    @PostMapping(path = "/secure/documents/upload-replace")
    @ApiMessage(message = "Upload và thay thế")
    public ResponseEntity<Document> uploadReplace(@Valid @ModelAttribute FileUploadReq fileUploadReq) throws IOException {
        Folder folder = null;
        Document existingDoc = null;

        if (fileUploadReq.getFolderId() != null) {
            folder = this.folderService.getFolderById(fileUploadReq.getFolderId());
            existingDoc = documentService.findByNameAndFolderAndIsDeletedFalse(fileUploadReq.getFile().getOriginalFilename(), folder);
            if (existingDoc == null) {
                throw new NotFoundException("Không tìm thấy file trong thư mục để thay thế");
            }
        } else {
            existingDoc = documentService.findByNameAndCreatedByAndFolderIsNullAndIsDeletedFalse(fileUploadReq.getFile().getOriginalFilename(),
                    SecurityUtil.getCurrentUserFromThreadLocal());
            if (existingDoc == null) {
                throw new NotFoundException("Không tìm thấy file để thay thể");
            }
        }

        Document doc = documentService.uploadReplaceFile(fileUploadReq.getFile(), folder, existingDoc);
        return ResponseEntity.status(HttpStatus.OK).body(doc);
    }

    @PostMapping(path = "/secure/documents/upload-keep")
    @ApiMessage(message = "Upload và giữ cả 2")
    public ResponseEntity<Document> uploadKeep(@Valid @ModelAttribute FileUploadReq fileUploadReq) throws IOException {
        Folder folder = fileUploadReq.getFolderId() != null ? folderService.getFolderById(fileUploadReq.getFolderId()) : null;
        Document doc = documentService.uploadKeepBothFiles(fileUploadReq.getFile(), folder);
        return ResponseEntity.status(HttpStatus.CREATED).body(doc);
    }


//    @PostMapping(path = "/secure/documents/upload-multiple")
//    @ApiMessage(message = "Upload nhiều tài liệu")
//    public ResponseEntity<List<Document>> uploadMultiple(
//            @Valid @ModelAttribute MultipleFileUploadReq fileUploadReq) throws IOException {
//
//        Folder folder = fileUploadReq.getFolderId() != null
//                ? folderService.getFolderById(fileUploadReq.getFolderId())
//                : null;
//
//        List<Document> result = new ArrayList<>();
//        for (MultipartFile file : fileUploadReq.getFiles()) {
//            Document doc = documentService.uploadFile(file, folder);
//            result.add(doc);
//        }
//
//        return ResponseEntity.status(HttpStatus.CREATED).body(result);
//    }

    @PatchMapping("/secure/documents/{id}")
    @ApiMessage(message = "Cập nhật tài liệu")
    public ResponseEntity<Document> update(@PathVariable Integer id, @Valid @RequestBody Document request) {
        Document doc = documentService.getDocumentById(id);
        if (doc == null || Boolean.TRUE.equals(doc.getDeleted())) {
            throw new NotFoundException("Tài liệu không tồn tại hoặc đã bị xoá.");
        }

        if (!this.documentPermissionService.checkCanEdit(SecurityUtil.getCurrentUserFromThreadLocal(), doc)) {
            throw new ForbiddenException("Bạn không có quyền chỉnh sửa/xoá tài liệu này");
        }

        if (doc.getFolder() != null) {
            if (documentService.existsByNameAndFolderAndIsDeletedFalseAndIdNot(request.getName(), doc.getFolder(), doc.getId())) {
                throw new UniqueConstraintException("Không thể đổi tên này trong cùng thư mục.");
            }
        } else {
            if (documentService.existsByNameAndCreatedByAndFolderIsNullAndIsDeletedFalseAndIdNot(request.getName(),
                    SecurityUtil.getCurrentUserFromThreadLocal(), doc.getId())) {
                throw new UniqueConstraintException("Không thể đổi tên này trong thư mục gốc.");
            }
        }

        doc.setName(request.getName());
        doc.setDescription(request.getDescription());
        return ResponseEntity.ok(this.documentService.save(doc));
    }

    @GetMapping(path = "/secure/documents/download/{storedFilename}")
    public ResponseEntity<byte[]> download(@PathVariable(value = "storedFilename") String storedFilename) {
        Document doc = this.documentService.getDocumentByStoredFilename(storedFilename);
        if (doc == null || Boolean.TRUE.equals(doc.getDeleted())) {
            throw new NotFoundException("Tài liệu không tồn tại hoặc đã bị xoá");
        }
        byte[] data = this.documentService.downloadFile(doc.getFilePath());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getOriginalFilename() + "\"")
                .contentType(MediaType.parseMediaType(doc.getMimeType()))
                .body(data);
    }

    @DeleteMapping("/secure/documents")
    @ApiMessage(message = "Chuyển tài liệu vào thùng rác")
    public ResponseEntity<Void> softDelete(@RequestBody List<Integer> ids) {
        List<Document> docs = this.documentService.getDocumentsByIds(ids);
        Map<Integer, Document> docsMap = docs.stream()
                .filter(doc -> !doc.getDeleted())
                .collect(Collectors.toMap(doc -> doc.getId(), doc -> doc));
        List<Integer> notFoundIds = ids.stream().filter(id -> !docsMap.containsKey(id)).toList();

        if (!notFoundIds.isEmpty()) {
            throw new NotFoundException("Không tìm thấy tài liệu với các ID (hoặc đã bị xóa mềm): " + notFoundIds);
        }

        for (Document doc : docs) {
            doc.setDeleted(true);
            this.documentService.save(doc);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @PatchMapping(path = "/secure/documents/restore")
    @ApiMessage(message = "Khôi phục tài liệu")
    public ResponseEntity<List<Document>> restore(@RequestBody List<Integer> ids) {
        List<Document> docs = this.documentService.getDocumentsByIds(ids);
        Map<Integer, Document> docsMap = docs.stream()
                .filter(doc -> doc.getDeleted())
                .collect(Collectors.toMap(doc -> doc.getId(), doc -> doc));
        List<Integer> notFoundIds = ids.stream().filter(id -> !docsMap.containsKey(id)).toList();

        if (!notFoundIds.isEmpty()) {
            throw new NotFoundException("Không tìm thấy tài liệu với các ID (hoặc chưa bị xóa mềm): " + notFoundIds);
        }

        for (Document doc : docs) {
            doc.setDeleted(false);
            this.documentService.save(doc);
        }
        return ResponseEntity.status(HttpStatus.OK).body(docs);
    }

    @DeleteMapping("/secure/documents/permanent")
    @ApiMessage(message = "Xoá vĩnh viễn tài liệu")
    public ResponseEntity<Void> hardDelete(@RequestBody List<Integer> ids) {
        List<Document> docs = this.documentService.getDocumentsByIds(ids);
        Map<Integer, Document> docsMap = docs.stream()
                .filter(doc -> doc.getDeleted())
                .collect(Collectors.toMap(doc -> doc.getId(), doc -> doc));
        List<Integer> notFoundIds = ids.stream().filter(id -> !docsMap.containsKey(id)).toList();

        if (!notFoundIds.isEmpty()) {
            throw new NotFoundException("Không tìm thấy tài liệu với các ID (hoặc chưa bị xóa mềm): " + notFoundIds);
        }

        for (Document doc : docs) {
            this.documentService.hardDelete(doc);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/secure/documents/copy")
    @ApiMessage(message = "Sao chép tài liệu")
    public ResponseEntity<Void> copyDocuments(@RequestBody List<Integer> ids, @RequestParam(required = false) Integer targetFolderId) {
        List<Document> docs = this.documentService.getDocumentsByIds(ids);
        Map<Integer, Document> docsMap = docs.stream()
                .filter(doc -> !doc.getDeleted())
                .collect(Collectors.toMap(doc -> doc.getId(), doc -> doc));
        List<Integer> notFoundIds = ids.stream().filter(id -> !docsMap.containsKey(id)).toList();

        if (!notFoundIds.isEmpty()) {
            throw new NotFoundException("Không tìm thấy tài liệu với các ID (hoặc đã bị xóa mềm): " + notFoundIds);
        }

        Folder folder = this.folderService.getFolderById(targetFolderId);
        if (folder == null || Boolean.TRUE.equals(folder.getDeleted())) {
            throw new NotFoundException("Thư mục không tồn tại hoặc đã bị xóa");
        }

        this.documentService.copyDocuments(docs, folder);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/secure/documents/move")
    @ApiMessage(message = "Di chuyển tài liệu")
    public ResponseEntity<Void> moveDocuments(@RequestBody List<Integer> ids, @RequestParam(required = false) Integer targetFolderId) {
        List<Document> docs = this.documentService.getDocumentsByIds(ids);
        Map<Integer, Document> docsMap = docs.stream()
                .filter(doc -> !doc.getDeleted())
                .collect(Collectors.toMap(doc -> doc.getId(), doc -> doc));
        List<Integer> notFoundIds = ids.stream().filter(id -> !docsMap.containsKey(id)).toList();

        if (!notFoundIds.isEmpty()) {
            throw new NotFoundException("Không tìm thấy tài liệu với các ID (hoặc đã bị xóa mềm): " + notFoundIds);
        }

        Folder folder = this.folderService.getFolderById(targetFolderId);
        if (folder == null || Boolean.TRUE.equals(folder.getDeleted())) {
            throw new NotFoundException("Thư mục không tồn tại hoặc đã bị xóa");
        }

        this.documentService.cutDocuments(docs, folder);
        return ResponseEntity.ok().build();
    }

}