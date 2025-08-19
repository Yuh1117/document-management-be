package com.vpgh.dms.controller;

import com.vpgh.dms.model.dto.DocumentDTO;
import com.vpgh.dms.model.dto.request.CopyCutReq;
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
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
    public ResponseEntity<List<Document>> upload(@Valid @ModelAttribute FileUploadReq fileUploadReq) throws IOException {
        Folder folder = null;
        if (fileUploadReq.getFolderId() != null) {
            folder = this.folderService.getFolderById(fileUploadReq.getFolderId());
            if (folder == null || Boolean.TRUE.equals(folder.getDeleted())) {
                throw new NotFoundException("Thư mục không tồn tại hoặc đã bị xóa");
            }
        }

        List<Document> uploadedDocs = new ArrayList<>();
        for (MultipartFile file : fileUploadReq.getFiles()) {
            String filename = file.getOriginalFilename();
            if (folder != null) {
                if (documentService.existsByNameAndFolderAndIsDeletedFalseAndIdNot(filename, folder, null)) {
                    throw new UniqueConstraintException("Tài liệu trùng tên trong cùng thư mục: " + filename);
                }
            } else {
                if (documentService.existsByNameAndCreatedByAndFolderIsNullAndIsDeletedFalseAndIdNot(filename,
                        SecurityUtil.getCurrentUserFromThreadLocal(), null)) {
                    throw new UniqueConstraintException("Tài liệu trùng tên trong thư mục gốc: " + filename);
                }
            }
            Document doc = this.documentService.uploadNewFile(file, folder);
            uploadedDocs.add(doc);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedDocs);
    }

    @PostMapping(path = "/secure/documents/upload-replace")
    @ApiMessage(message = "Upload và thay thế")
    public ResponseEntity<List<Document>> uploadReplace(@Valid @ModelAttribute FileUploadReq fileUploadReq) throws IOException {
        Folder folder = null;
        if (fileUploadReq.getFolderId() != null) {
            folder = this.folderService.getFolderById(fileUploadReq.getFolderId());
            if (folder == null || Boolean.TRUE.equals(folder.getDeleted())) {
                throw new NotFoundException("Thư mục không tồn tại hoặc đã bị xóa");
            }
        }

        List<Document> replacedDocs = new ArrayList<>();
        for (MultipartFile file : fileUploadReq.getFiles()) {
            String filename = file.getOriginalFilename();
            Document existingDoc;

            if (folder != null) {
                existingDoc = documentService.findByNameAndFolderAndIsDeletedFalse(filename, folder);
                if (existingDoc == null) {
                    throw new NotFoundException("Không tìm thấy file trong thư mục để thay thế: " + filename);
                }
            } else {
                existingDoc = documentService.findByNameAndCreatedByAndFolderIsNullAndIsDeletedFalse(
                        filename, SecurityUtil.getCurrentUserFromThreadLocal());
                if (existingDoc == null) {
                    throw new NotFoundException("Không tìm thấy file để thay thể: " + filename);
                }
            }

            Document doc = documentService.uploadReplaceFile(file, folder, existingDoc);
            replacedDocs.add(doc);
        }

        return ResponseEntity.status(HttpStatus.OK).body(replacedDocs);
    }

    @PostMapping(path = "/secure/documents/upload-keep")
    @ApiMessage(message = "Upload và giữ cả 2")
    public ResponseEntity<List<Document>> uploadKeep(@Valid @ModelAttribute FileUploadReq fileUploadReq) throws IOException {
        Folder folder = null;
        if (fileUploadReq.getFolderId() != null) {
            folder = this.folderService.getFolderById(fileUploadReq.getFolderId());
            if (folder == null || Boolean.TRUE.equals(folder.getDeleted())) {
                throw new NotFoundException("Thư mục không tồn tại hoặc đã bị xóa");
            }
        }

        List<Document> uploadedDocs = new ArrayList<>();
        for (MultipartFile file : fileUploadReq.getFiles()) {
            Document doc = documentService.uploadKeepBothFiles(file, folder);
            uploadedDocs.add(doc);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedDocs);
    }


    @PatchMapping("/secure/documents/{id}")
    @ApiMessage(message = "Cập nhật tài liệu")
    public ResponseEntity<Document> update(@PathVariable Integer id, @Valid @RequestBody Document request) {
        Document doc = documentService.getDocumentById(id);
        if (doc == null || Boolean.TRUE.equals(doc.getDeleted())) {
            throw new NotFoundException("Tài liệu không tồn tại hoặc đã bị xoá.");
        }

        if (!this.documentPermissionService.checkCanEdit(SecurityUtil.getCurrentUserFromThreadLocal(), doc)) {
            throw new ForbiddenException("Bạn không có quyền chỉnh sửa tài liệu này");
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

    @GetMapping(path = "/secure/documents/download/{id}")
    public ResponseEntity<InputStreamResource> download(@PathVariable Integer id) {
        Document doc = this.documentService.getDocumentById(id);
        if (doc == null || Boolean.TRUE.equals(doc.getDeleted())) {
            throw new NotFoundException("Tài liệu không tồn tại hoặc đã bị xoá");
        }

        InputStream inputStream = documentService.downloadFileStream(doc.getFilePath());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getName() + "\"")
                .contentType(MediaType.parseMediaType(doc.getMimeType()))
                .body(new InputStreamResource(inputStream));
    }

    @PostMapping("/secure/documents/download/multiple")
    public ResponseEntity<StreamingResponseBody> downloadMultiple(@RequestBody List<Integer> ids) throws IOException {
        List<Document> docs = this.documentService.getDocumentsByIds(ids);
        Map<Integer, Document> docsMap = docs.stream()
                .filter(doc -> !doc.getDeleted())
                .collect(Collectors.toMap(doc -> doc.getId(), doc -> doc));
        List<Integer> notFoundIds = ids.stream().filter(id -> !docsMap.containsKey(id)).toList();

        if (!notFoundIds.isEmpty()) {
            throw new NotFoundException("Không tìm thấy tài liệu với các ID (hoặc đã bị xóa mềm): " + notFoundIds);
        }

        StreamingResponseBody stream = outputStream -> {
            try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {
                for (Document doc : docs) {
                    try (InputStream in = documentService.downloadFileStream(doc.getFilePath())) {
                        ZipEntry entry = new ZipEntry(doc.getName());
                        zipOut.putNextEntry(entry);
                        in.transferTo(zipOut);
                        zipOut.closeEntry();
                    }
                }
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"documents.zip\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(stream);
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
    public ResponseEntity<Void> copyDocuments(@RequestBody CopyCutReq request) {
        List<Document> docs = this.documentService.getDocumentsByIds(request.getIds());
        Map<Integer, Document> docsMap = docs.stream()
                .filter(doc -> !doc.getDeleted())
                .collect(Collectors.toMap(doc -> doc.getId(), doc -> doc));
        List<Integer> notFoundIds = request.getIds().stream().filter(id -> !docsMap.containsKey(id)).toList();

        if (!notFoundIds.isEmpty()) {
            throw new NotFoundException("Không tìm thấy tài liệu với các ID (hoặc đã bị xóa mềm): " + notFoundIds);
        }

        Folder targetFolder = null;
        if (request.getTargetFolderId() != null) {
            targetFolder = this.folderService.getFolderById(request.getTargetFolderId());
            if (targetFolder == null || Boolean.TRUE.equals(targetFolder.getDeleted())) {
                throw new NotFoundException("Thư mục không tồn tại hoặc đã bị xóa");
            }
        }

        for (Document doc : docs) {
            this.documentService.copyDocument(doc, targetFolder);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/secure/documents/move")
    @ApiMessage(message = "Di chuyển tài liệu")
    public ResponseEntity<Void> moveDocuments(@RequestBody CopyCutReq request) {
        List<Document> docs = this.documentService.getDocumentsByIds(request.getIds());
        Map<Integer, Document> docsMap = docs.stream()
                .filter(doc -> !doc.getDeleted())
                .collect(Collectors.toMap(doc -> doc.getId(), doc -> doc));
        List<Integer> notFoundIds = request.getIds().stream().filter(id -> !docsMap.containsKey(id)).toList();

        if (!notFoundIds.isEmpty()) {
            throw new NotFoundException("Không tìm thấy tài liệu với các ID (hoặc đã bị xóa mềm): " + notFoundIds);
        }

        Folder targetFolder = null;
        if (request.getTargetFolderId() != null) {
            targetFolder = this.folderService.getFolderById(request.getTargetFolderId());
            if (targetFolder == null || Boolean.TRUE.equals(targetFolder.getDeleted())) {
                throw new NotFoundException("Thư mục không tồn tại hoặc đã bị xóa");
            }
        }

        for (Document doc : docs) {
            this.documentService.moveDocument(doc, targetFolder);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/secure/documents/{id}")
    @ApiMessage(message = "Xem tài liệu")
    public ResponseEntity<DocumentDTO> detail(@PathVariable Integer id) {
        Document doc = this.documentService.getDocumentById(id);
        if (doc == null || Boolean.TRUE.equals(doc.getDeleted())) {
            throw new NotFoundException("Tài liệu không tồn tại hoặc đã bị xóa");
        }

        if (!this.documentPermissionService.checkCanView(SecurityUtil.getCurrentUserFromThreadLocal(), doc)) {
            throw new ForbiddenException("Bạn không có quyền xem tài liệu này");
        }

        return ResponseEntity.status(HttpStatus.OK).body(this.documentService.convertDocumentToDocumentDTO(doc));
    }
}