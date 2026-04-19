package com.vpgh.dms.controller;

import com.vpgh.dms.model.dto.DocumentDTO;
import com.vpgh.dms.model.dto.request.*;
import com.vpgh.dms.model.entity.*;
import com.vpgh.dms.service.*;
import com.vpgh.dms.util.DataResponse;
import com.vpgh.dms.util.SecurityUtil;
import com.vpgh.dms.util.annotation.ApiMessage;
import com.vpgh.dms.util.exception.FileException;
import com.vpgh.dms.util.exception.ForbiddenException;
import com.vpgh.dms.util.exception.NotFoundException;
import com.vpgh.dms.util.exception.UniqueConstraintException;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api")
public class DocumentController {
    private final DocumentService documentService;
    private final DocumentShareService documentShareService;
    private final FolderService folderService;
    private final FolderShareService folderShareService;
    private final StegoService stegoService;
    private final Executor uploadExecutor;

    public DocumentController(DocumentService documentService, DocumentShareService documentShareService,
            FolderService folderService, FolderShareService folderShareService, StegoService stegoService,
            @Qualifier("uploadExecutor") Executor uploadExecutor) {
        this.documentService = documentService;
        this.documentShareService = documentShareService;
        this.folderService = folderService;
        this.folderShareService = folderShareService;
        this.stegoService = stegoService;
        this.uploadExecutor = uploadExecutor;
    }

    @PostMapping(path = "/secure/documents/upload")
    @ApiMessage(key = "api.document.upload", message = "Upload document")
    public ResponseEntity<Map<String, Object>> upload(@Valid @ModelAttribute FileUploadReq fileUploadReq)
            throws IOException {
        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();
        Folder folder = resolveUploadFolder(fileUploadReq.getFolderId(), currentUser);
        List<MultipartFile> files = fileUploadReq.getFiles();

        List<CompletableFuture<Document>> futures = new ArrayList<>();
        for (MultipartFile file : files) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                String filename = file.getOriginalFilename();
                boolean conflict = folder != null
                        ? documentService.existsByNameAndFolderAndIsDeletedFalseAndIdNot(filename, folder, null)
                        : documentService.existsByNameAndCreatedByAndFolderIsNullAndIsDeletedFalseAndIdNot(filename,
                                currentUser, null);
                if (conflict)
                    return null;
                try {
                    Document doc = documentService.uploadNewFile(file, folder);
                    if (doc.getFolder() != null) {
                        documentShareService.handleShareAfterUpload(folder, doc);
                    }
                    return doc;
                } catch (IOException e) {
                    throw new CompletionException(e);
                }
            }, uploadExecutor));
        }

        List<Document> uploaded = new ArrayList<>();
        List<String> conflicts = new ArrayList<>();
        try {
            for (int i = 0; i < futures.size(); i++) {
                Document doc = futures.get(i).join();
                if (doc == null)
                    conflicts.add(files.get(i).getOriginalFilename());
                else
                    uploaded.add(doc);
            }
        } catch (CompletionException e) {
            if (e.getCause() instanceof IOException io)
                throw io;
            throw e;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("uploaded", uploaded);
        response.put("conflicts", conflicts);
        return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(response);
    }

    @PostMapping(path = "/secure/documents/upload-replace")
    @ApiMessage(key = "api.document.uploadReplace", message = "Upload and replace")
    public ResponseEntity<List<Document>> uploadReplace(@Valid @ModelAttribute FileUploadReq fileUploadReq)
            throws IOException {
        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();
        Folder folder = resolveUploadFolder(fileUploadReq.getFolderId(), currentUser);
        List<MultipartFile> files = fileUploadReq.getFiles();

        List<Document> existingDocs = new ArrayList<>();
        for (MultipartFile file : files) {
            String filename = file.getOriginalFilename();
            Document existing = folder != null
                    ? documentService.findByNameAndFolderAndIsDeletedFalse(filename, folder)
                    : documentService.findByNameAndCreatedByAndFolderIsNullAndIsDeletedFalse(filename, currentUser);
            if (existing == null) {
                String errorKey = folder != null ? "error.document.replace.notInFolder"
                        : "error.document.replace.notFound";
                throw new NotFoundException(errorKey, filename);
            }
            existingDocs.add(existing);
        }

        List<CompletableFuture<Document>> futures = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            Document existing = existingDocs.get(i);
            futures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    return documentService.uploadReplaceFile(file, folder, existing);
                } catch (IOException e) {
                    throw new CompletionException(e);
                }
            }, uploadExecutor));
        }

        List<Document> replaced = new ArrayList<>();
        try {
            for (CompletableFuture<Document> f : futures)
                replaced.add(f.join());
        } catch (CompletionException e) {
            if (e.getCause() instanceof IOException io)
                throw io;
            throw e;
        }
        return ResponseEntity.status(HttpStatus.OK).body(replaced);
    }

    @PostMapping(path = "/secure/documents/upload-keep")
    @ApiMessage(key = "api.document.uploadKeepBoth", message = "Upload and keep both")
    public ResponseEntity<List<Document>> uploadKeep(@Valid @ModelAttribute FileUploadReq fileUploadReq)
            throws IOException {
        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();
        Folder folder = resolveUploadFolder(fileUploadReq.getFolderId(), currentUser);
        List<MultipartFile> files = fileUploadReq.getFiles();

        List<CompletableFuture<Document>> futures = files.stream()
                .map(file -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return documentService.uploadKeepBothFiles(file, folder);
                    } catch (IOException e) {
                        throw new CompletionException(e);
                    }
                }, uploadExecutor))
                .toList();

        List<Document> uploaded = new ArrayList<>();
        try {
            for (CompletableFuture<Document> f : futures)
                uploaded.add(f.join());
        } catch (CompletionException e) {
            if (e.getCause() instanceof IOException io)
                throw io;
            throw e;
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(uploaded);
    }

    @PatchMapping("/secure/documents/{id}")
    @ApiMessage(key = "api.document.update", message = "Update document")
    public ResponseEntity<DocumentDTO> update(@PathVariable Integer id, @Valid @RequestBody Document request) {
        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();
        Document doc = resolveDocumentForEdit(id, currentUser);

        if (doc.getFolder() != null) {
            if (documentService.existsByNameAndFolderAndIsDeletedFalseAndIdNot(request.getName(), doc.getFolder(),
                    doc.getId())) {
                throw new UniqueConstraintException("error.unique.renameInParent");
            }
        } else {
            if (documentService.existsByNameAndCreatedByAndFolderIsNullAndIsDeletedFalseAndIdNot(request.getName(),
                    currentUser, doc.getId())) {
                throw new UniqueConstraintException("error.unique.renameInRoot");
            }
        }

        Document updated = this.documentService.handleUpdateDocument(doc, request.getName(), request.getDescription());
        return ResponseEntity.ok(this.documentService.convertDocumentToDocumentDTO(updated));
    }

    @GetMapping(path = "/secure/documents/download/{id}")
    public ResponseEntity<InputStreamResource> download(@PathVariable Integer id) {
        Document doc = resolveDocumentForView(id, SecurityUtil.getCurrentUserFromThreadLocal());
        InputStream inputStream = documentService.downloadFileStream(doc.getFilePath());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + URLEncoder.encode(doc.getName(), StandardCharsets.UTF_8) + "\"")
                .contentType(MediaType.parseMediaType(doc.getMimeType()))
                .body(new InputStreamResource(inputStream));
    }

    @PostMapping("/secure/documents/download/multiple")
    public ResponseEntity<StreamingResponseBody> downloadMultiple(@RequestBody List<Integer> ids) throws IOException {
        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();
        List<Document> docs = resolveViewableDocuments(ids, currentUser);

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

    @PatchMapping("/secure/documents")
    @ApiMessage(key = "api.document.trash", message = "Move document to trash")
    public ResponseEntity<Void> softDelete(@RequestBody List<Integer> ids) {
        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();
        List<Document> docs = resolveOwnedActiveDocuments(ids, currentUser);

        for (Document doc : docs) {
            doc.setDeleted(true);
            this.documentService.save(doc);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping(path = "/secure/documents/restore")
    @ApiMessage(key = "api.document.restore", message = "Restore document")
    public ResponseEntity<List<Document>> restore(@RequestBody List<Integer> ids) {
        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();
        List<Document> docs = resolveOwnedTrashedDocuments(ids, currentUser);

        for (Document doc : docs) {
            doc.setDeleted(false);
            this.documentService.save(doc);
        }
        return ResponseEntity.status(HttpStatus.OK).body(docs);
    }

    @DeleteMapping("/secure/documents/permanent")
    @ApiMessage(key = "api.document.permanentDelete", message = "Permanently delete document")
    public ResponseEntity<Void> hardDelete(@RequestBody List<Integer> ids) {
        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();
        List<Document> docs = resolveOwnedTrashedDocuments(ids, currentUser);

        for (Document doc : docs) {
            this.documentService.hardDelete(doc);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/secure/documents/copy")
    @ApiMessage(key = "api.document.copy", message = "Copy document")
    public ResponseEntity<Void> copyDocuments(@RequestBody CopyCutReq request) {
        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();
        List<Document> docs = resolveViewableDocuments(request.getIds(), currentUser);
        Folder targetFolder = resolveTargetFolder(request.getTargetFolderId());

        for (Document doc : docs) {
            this.documentService.copyDocument(doc, targetFolder);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/secure/documents/move")
    @ApiMessage(key = "api.document.move", message = "Move document")
    public ResponseEntity<Void> moveDocuments(@RequestBody CopyCutReq request) {
        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();
        List<Document> docs = resolveOwnedActiveDocuments(request.getIds(), currentUser);
        Folder targetFolder = resolveTargetFolder(request.getTargetFolderId());

        for (Document doc : docs) {
            if (doc.getFolder() == null) {
                if (targetFolder == null)
                    continue;
            } else {
                if (doc.getFolder().equals(targetFolder))
                    continue;
            }
            this.documentService.moveDocument(doc, targetFolder);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/secure/documents/{id}")
    @ApiMessage(key = "api.document.detail", message = "View document details")
    public ResponseEntity<DocumentDTO> detail(@PathVariable Integer id) {
        Document doc = resolveDocumentForView(id, SecurityUtil.getCurrentUserFromThreadLocal());
        return ResponseEntity.ok(this.documentService.convertDocumentToDocumentDTO(doc));
    }

    @GetMapping(path = "/secure/documents/{id}/preview")
    @ApiMessage(key = "api.document.view", message = "View document")
    public ResponseEntity<InputStreamResource> preview(@PathVariable Integer id) {
        Document doc = resolveDocumentForView(id, SecurityUtil.getCurrentUserFromThreadLocal());
        InputStream inputStream = documentService.downloadFileStream(doc.getFilePath());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + URLEncoder.encode(doc.getName(), StandardCharsets.UTF_8) + "\"")
                .contentType(MediaType.parseMediaType(doc.getMimeType()))
                .body(new InputStreamResource(inputStream));
    }

    @PostMapping(path = "/secure/documents/share-url")
    @ApiMessage(key = "api.document.signedUrl", message = "Create signed URL")
    public ResponseEntity<DataResponse<String>> getSignedUrl(@Valid @RequestBody SignedUrlRequest request) {
        Document doc = resolveDocumentForView(request.getDocumentId(),
                SecurityUtil.getCurrentUserFromThreadLocal());

        String url = this.documentService.generateSignedUrl(doc, Integer.parseInt(request.getExpiredTime()));
        DataResponse<String> res = new DataResponse<>();
        res.setContent(url);

        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate, max-age=0")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.EXPIRES, "0")
                .body(res);
    }

    @PostMapping(path = "/secure/documents/hide-data")
    @ApiMessage(key = "api.document.hideData", message = "Hide data")
    public ResponseEntity<InputStreamResource> hideData(@Valid @ModelAttribute HideDataReq request) throws Exception {
        if (!"application/pdf".equals(request.getFile().getContentType())) {
            throw new FileException("error.file.invalidType");
        }

        ByteArrayOutputStream out = stegoService.hideData(request.getFile().getInputStream(), request.getContent(),
                request.getPassword());
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\""
                                + URLEncoder.encode(request.getFile().getOriginalFilename(), StandardCharsets.UTF_8)
                                + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(in));
    }

    @PostMapping(path = "/secure/documents/extract-data")
    @ApiMessage(key = "api.document.extractData", message = "Extract data")
    public ResponseEntity<DataResponse<String>> extractData(@Valid @ModelAttribute ExtractDataReq request)
            throws Exception {
        if (!"application/pdf".equals(request.getFile().getContentType())) {
            throw new FileException("error.file.invalidType");
        }

        String content = stegoService.extractData(request.getFile().getInputStream(), request.getPassword());
        if (content == null) {
            throw new FileException("error.stego.hiddenDataNotFound");
        }

        return ResponseEntity.ok(new DataResponse<>(content));
    }

    private Folder resolveUploadFolder(Integer folderId, User user) {
        if (folderId == null)
            return null;
        Folder folder = folderService.getFolderById(folderId);
        if (folder == null || Boolean.TRUE.equals(folder.getDeleted())) {
            throw new NotFoundException("error.folder.notFoundOrDeleted");
        }
        if (!folderShareService.checkCanEdit(user, folder)) {
            throw new ForbiddenException("error.forbidden.uploadDocumentHere");
        }
        return folder;
    }

    private Folder resolveTargetFolder(Integer folderId) {
        if (folderId == null)
            return null;
        Folder folder = folderService.getFolderById(folderId);
        if (folder == null || Boolean.TRUE.equals(folder.getDeleted())) {
            throw new NotFoundException("error.folder.notFoundOrDeleted");
        }
        return folder;
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

    private List<Document> resolveViewableDocuments(List<Integer> ids, User user) {
        List<Document> docs = documentService.getDocumentsByIds(ids);
        Map<Integer, Document> docsMap = docs.stream()
                .filter(doc -> !doc.getDeleted() && documentShareService.checkCanView(user, doc))
                .collect(Collectors.toMap(Document::getId, doc -> doc));
        List<Integer> notFoundIds = ids.stream().filter(id -> !docsMap.containsKey(id)).toList();
        if (!notFoundIds.isEmpty()) {
            throw new NotFoundException("error.document.idsSoftDeleted", notFoundIds);
        }
        return docs;
    }

    private List<Document> resolveOwnedActiveDocuments(List<Integer> ids, User user) {
        List<Document> docs = documentService.getDocumentsByIds(ids);
        Map<Integer, Document> docsMap = docs.stream()
                .filter(doc -> !doc.getDeleted() && documentService.isOwnerDocument(doc, user))
                .collect(Collectors.toMap(Document::getId, doc -> doc));
        List<Integer> notFoundIds = ids.stream().filter(id -> !docsMap.containsKey(id)).toList();
        if (!notFoundIds.isEmpty()) {
            throw new NotFoundException("error.document.idsSoftDeleted", notFoundIds);
        }
        return docs;
    }

    private List<Document> resolveOwnedTrashedDocuments(List<Integer> ids, User user) {
        List<Document> docs = documentService.getDocumentsByIds(ids);
        Map<Integer, Document> docsMap = docs.stream()
                .filter(doc -> doc.getDeleted() && documentService.isOwnerDocument(doc, user))
                .collect(Collectors.toMap(Document::getId, doc -> doc));
        List<Integer> notFoundIds = ids.stream().filter(id -> !docsMap.containsKey(id)).toList();
        if (!notFoundIds.isEmpty()) {
            throw new NotFoundException("error.document.idsNotSoftDeleted", notFoundIds);
        }
        return docs;
    }

}