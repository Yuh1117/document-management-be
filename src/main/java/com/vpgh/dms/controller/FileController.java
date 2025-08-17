package com.vpgh.dms.controller;

import com.vpgh.dms.model.dto.response.FileItemDTO;
import com.vpgh.dms.model.dto.response.FileResponse;
import com.vpgh.dms.model.dto.response.PaginationResDTO;
import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.Folder;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.service.DocumentService;
import com.vpgh.dms.service.FileService;
import com.vpgh.dms.service.FolderService;
import com.vpgh.dms.util.SecurityUtil;
import com.vpgh.dms.util.annotation.ApiMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FileController {

    @Autowired
    private FolderService folderService;
    @Autowired
    private DocumentService documentService;
    @Autowired
    private FileService fileService;

    @GetMapping(path = "/secure/my-files")
    @ApiMessage(message = "Lấy files của tôi")
    public ResponseEntity<PaginationResDTO<List<FileItemDTO>>> getMyDrive(@RequestParam Map<String, String> params) {
        String page = params.get("page");
        if (page == null || page.isEmpty()) {
            params.put("page", "1");
        }

        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();
        PaginationResDTO<List<FileItemDTO>> files = fileService.getUserFiles(currentUser, -1, params);
        return ResponseEntity.ok(files);
    }

    @GetMapping(path = "/secure/search")
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
}
