package com.vpgh.dms.controller;

import com.vpgh.dms.model.dto.response.FileItemDTO;
import com.vpgh.dms.model.dto.response.FileResponse;
import com.vpgh.dms.model.dto.response.PaginationResDTO;
import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.Folder;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.service.DocumentService;
import com.vpgh.dms.service.FileService;
import com.vpgh.dms.service.FolderShareService;
import com.vpgh.dms.service.FolderService;
import com.vpgh.dms.util.SecurityUtil;
import com.vpgh.dms.util.annotation.ApiMessage;
import com.vpgh.dms.util.exception.ForbiddenException;
import com.vpgh.dms.util.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api")
public class FileController {

    @Autowired
    private FolderService folderService;
    @Autowired
    private DocumentService documentService;
    @Autowired
    private FileService fileService;
    @Autowired
    private FolderShareService folderShareService;

    @GetMapping(path = "/secure/files/my-files")
    @ApiMessage(message = "Lấy files của tôi")
    public ResponseEntity<PaginationResDTO<List<FileItemDTO>>> getMyDrive(@RequestParam Map<String, String> params) {
        String page = params.get("page");
        if (page == null || page.isEmpty()) {
            params.put("page", "1");
        }

        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();
        Page<FileItemDTO> items = fileService.getUserFiles(currentUser, -1, params);
        List<FileItemDTO> files = items.getContent();

        PaginationResDTO<List<FileItemDTO>> res = new PaginationResDTO<>();
        res.setResult(files);
        res.setCurrentPage(items.getNumber() + 1);
        res.setTotalPages(items.getTotalPages());
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @GetMapping(path = "/secure/files/trash")
    @ApiMessage(message = "Lấy files trong thùng rác")
    public ResponseEntity<PaginationResDTO<List<FileItemDTO>>> getTrashFiles(@RequestParam Map<String, String> params) {
        String page = params.get("page");
        if (page == null || page.isEmpty()) {
            params.put("page", "1");
        }

        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();
        Page<FileItemDTO> items = fileService.getTrashFiles(currentUser, -1, params);
        List<FileItemDTO> files = items.getContent();

        PaginationResDTO<List<FileItemDTO>> res = new PaginationResDTO<>();
        res.setResult(files);
        res.setCurrentPage(items.getNumber() + 1);
        res.setTotalPages(items.getTotalPages());
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @GetMapping(path = "/secure/files/search")
    @ApiMessage(message = "Tìm kiếm")
    public ResponseEntity<FileResponse> search(@RequestParam Map<String, String> params) {
        String page = params.get("page");
        if (page == null || page.isEmpty()) {
            params.put("page", "1");
        }

        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();
        Page<Folder> folders = this.folderService.searchFolders(params, currentUser);
        Page<Document> documents = this.documentService.searchDocuments(params, currentUser);

        return ResponseEntity.status(HttpStatus.OK).body(new FileResponse(this.folderService.convertFoldersToFolderDTOs(folders.getContent()),
                this.documentService.convertDocumentsToDocumentDTOs(documents.getContent())));
    }

    @GetMapping("/secure/files/folders/{id}")
    @ApiMessage(message = "Lấy files trong folder")
    public ResponseEntity<PaginationResDTO<List<FileItemDTO>>> getFolderFiles(@PathVariable("id") Integer id, @RequestParam Map<String, String> params) {
        Folder folder = this.folderService.getFolderById(id);
        if (folder == null || Boolean.TRUE.equals(folder.getDeleted())) {
            throw new NotFoundException("Thư mục không tồn tại hoặc đã bị xóa");
        }

        if (!this.folderShareService.checkCanView(SecurityUtil.getCurrentUserFromThreadLocal(), folder)) {
            throw new ForbiddenException("Bạn không có quyền xem thư mục này");
        }

        String page = params.get("page");
        if (page == null || page.isEmpty()) {
            params.put("page", "1");
        }

        Page<FileItemDTO> items = fileService.getFolderFiles(id, params);
        List<FileItemDTO> files = items.getContent();

        PaginationResDTO<List<FileItemDTO>> res = new PaginationResDTO<>();
        res.setResult(files);
        res.setCurrentPage(items.getNumber() + 1);
        res.setTotalPages(items.getTotalPages());

        return ResponseEntity.ok(res);
    }

    @PostMapping("/secure/files/download/multiple")
    public ResponseEntity<StreamingResponseBody> downloadMixed(@RequestBody Map<String, List<Integer>> request) {
        List<Folder> folders = folderService.getFoldersByIds(request.get("folderIds"));
        List<Document> docs = documentService.getDocumentsByIds(request.get("documentIds"));

        Map<Integer, Folder> folderMap = folders.stream()
                .filter(f -> !f.getDeleted())
                .collect(Collectors.toMap(Folder::getId, f -> f));
        Map<Integer, Document> docMap = docs.stream()
                .filter(d -> !d.getDeleted())
                .collect(Collectors.toMap(Document::getId, d -> d));

        List<Integer> notFoundFolders = request.get("folderIds").stream()
                .filter(id -> !folderMap.containsKey(id))
                .toList();
        List<Integer> notFoundDocs = request.get("documentIds").stream()
                .filter(id -> !docMap.containsKey(id))
                .toList();

        if (!notFoundFolders.isEmpty() || !notFoundDocs.isEmpty()) {
            throw new NotFoundException("Không tìm thấy: folderIds=" + notFoundFolders + ", docIds=" + notFoundDocs);
        }

        StreamingResponseBody stream = outputStream -> {
            try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {
                for (Folder folder : folders) {
                    folderService.zipFolderIterative(folder, zipOut);
                }
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
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"folders-documents.zip\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(stream);
    }
}
