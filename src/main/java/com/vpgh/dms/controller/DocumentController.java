package com.vpgh.dms.controller;

import com.vpgh.dms.model.dto.request.FileUploadReq;
import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.Folder;
import com.vpgh.dms.service.DocumentService;
import com.vpgh.dms.service.FolderService;
import com.vpgh.dms.util.annotation.ApiMessage;
import com.vpgh.dms.util.exception.NotFoundException;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class DocumentController {
    @Autowired
    private DocumentService documentService;
    @Autowired
    private FolderService folderService;

    @PostMapping(path = "/secure/documents/upload")
    @ApiMessage(message = "Upload tài liệu")
    public ResponseEntity<Document> upload(@Valid @ModelAttribute FileUploadReq fileUploadReq) throws IOException {
        Folder folder = fileUploadReq.getFolderId() != null ? this.folderService.getFolderById(fileUploadReq.getFolderId()) : null;
        Document doc = this.documentService.uploadFile(fileUploadReq.getFile(), folder);
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

    @DeleteMapping(path = "/secure/documents/{id}")
    @ApiMessage(message = "Chuyển tài liệu vào thùng rác")
    public ResponseEntity<Void> softDelete(@PathVariable Integer id) {
        Document doc = this.documentService.getDocumentById(id);
        if (doc == null || Boolean.TRUE.equals(doc.getDeleted())) {
            throw new NotFoundException("Tài liệu không tồn tại hoặc đã bị xoá");
        }

        doc.setDeleted(true);
        this.documentService.save(doc);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/secure/documents")
    @ApiMessage(message = "Chuyển nhiều tài liệu vào thùng rác")
    public ResponseEntity<Void> softDeleteMany(@RequestBody List<Integer> ids) {
        List<Document> docs = this.documentService.getDocumentsByIds(ids);
        Map<Integer, Document> docsMap = docs.stream().collect(Collectors.toMap(Document::getId, Function.identity()));

        List<Integer> notFoundIds = ids.stream().filter(id -> !docsMap.containsKey(id)).toList();

        if (!notFoundIds.isEmpty()) {
            throw new NotFoundException("Không tìm thấy tài liệu với các ID: " + notFoundIds);
        }

        for (Document doc : docs) {
            if (doc != null && !Boolean.TRUE.equals(doc.getDeleted())) {
                doc.setDeleted(true);
                this.documentService.save(doc);
            }
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @PatchMapping(path = "/secure/documents/restore/{id}")
    @ApiMessage(message = "Khôi phục tài liệu")
    public ResponseEntity<Document> restore(@PathVariable Integer id) {
        Document doc = this.documentService.getDocumentById(id);
        if (doc == null || !Boolean.TRUE.equals(doc.getDeleted())) {
            throw new NotFoundException("Tài liệu không tồn tại hoặc chưa bị xoá mềm");
        }

        doc.setDeleted(false);
        doc = this.documentService.save(doc);
        return ResponseEntity.status(HttpStatus.OK).body(doc);
    }

    @DeleteMapping("/secure/documents/permanent/{id}")
    @ApiMessage(message = "Xoá vĩnh viễn tài liệu")
    public ResponseEntity<Void> hardDelete(@PathVariable Integer id) {
        Document doc = this.documentService.getDocumentById(id);
        if (doc == null || !Boolean.TRUE.equals(doc.getDeleted())) {
            throw new NotFoundException("Tài liệu không tồn tại hoặc chưa bị xoá mềm");
        }

        this.documentService.hardDelete(doc);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/secure/documents/permanent")
    @ApiMessage(message = "Xoá vĩnh viễn nhiều tài liệu")
    public ResponseEntity<Void> hardDeleteMany(@RequestBody List<Integer> ids) {
        List<Document> docs = this.documentService.getDocumentsByIds(ids);
        Map<Integer, Document> docsMap = docs.stream().collect(Collectors.toMap(Document::getId, Function.identity()));

        List<Integer> notFoundIds = ids.stream().filter(id -> !docsMap.containsKey(id)).toList();

        if (!notFoundIds.isEmpty()) {
            throw new NotFoundException("Không tìm thấy tài liệu với các ID: " + notFoundIds);
        }

        for (Document doc : docs) {
            if (doc != null && Boolean.TRUE.equals(doc.getDeleted())) {
                this.documentService.hardDelete(doc);
            }
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}