package com.vpgh.dms.controller;

import com.vpgh.dms.model.dto.FolderDTO;
import com.vpgh.dms.model.dto.request.CopyCutReq;
import com.vpgh.dms.model.dto.request.FolderUploadReq;
import com.vpgh.dms.model.entity.Folder;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.service.DocumentService;
import com.vpgh.dms.service.FolderShareService;
import com.vpgh.dms.service.FolderService;
import com.vpgh.dms.util.SecurityUtil;
import com.vpgh.dms.util.annotation.ApiMessage;
import com.vpgh.dms.util.exception.FileException;
import com.vpgh.dms.util.exception.ForbiddenException;
import com.vpgh.dms.util.exception.NotFoundException;
import com.vpgh.dms.util.exception.UniqueConstraintException;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api")
public class FolderController {
    private final FolderService folderService;
    private final FolderShareService folderShareService;

    public FolderController(FolderService folderService, FolderShareService folderShareService, DocumentService documentService) {
        this.folderService = folderService;
        this.folderShareService = folderShareService;
    }

    @PostMapping(path = "/secure/folders")
    @ApiMessage(key = "api.folder.create", message = "Create folder")
    public ResponseEntity<Folder> create(@RequestBody @Valid Folder folder) {
        if (folder.getParent() != null && folder.getParent().getId() != null) {
            Folder f = this.folderService.getFolderById(folder.getParent().getId());
            if (f == null || Boolean.TRUE.equals(folder.getDeleted())) {
                throw new NotFoundException("error.folder.notFoundOrDeleted");
            }
            if (!this.folderShareService.checkCanEdit(SecurityUtil.getCurrentUserFromThreadLocal(), f)) {
                throw new ForbiddenException("error.forbidden.createFolderHere");
            }
            folder.setParent(f);
        }

        if (folder.getParent() != null) {
            if (folderService.existsByNameAndParentAndIsDeletedFalseAndIdNot(folder.getName(), folder.getParent(), folder.getId())) {
                throw new UniqueConstraintException("error.unique.folderNameInParent");
            }
        } else {
            if (folderService.existsByNameAndCreatedByAndParentIsNullAndIsDeletedFalseAndIdNot(folder.getName(),
                    SecurityUtil.getCurrentUserFromThreadLocal(), folder.getId())) {
                throw new UniqueConstraintException("error.unique.folderInRoot");
            }
        }

        Folder currentFolder = this.folderService.save(folder);
        if (currentFolder.getParent() != null) {
            this.folderShareService.handleShareAfterCreate(currentFolder.getParent(), currentFolder);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(currentFolder);
    }

    @PostMapping(path = "/secure/folders/upload")
    @ApiMessage(key = "api.folder.upload", message = "Upload folder")
    public ResponseEntity<Folder> uploadFolder(@Valid @ModelAttribute FolderUploadReq folderUploadReq) throws IOException {
        Folder parentFolder = null;
        if (folderUploadReq.getParentId() != null) {
            parentFolder = this.folderService.getFolderById(folderUploadReq.getParentId());
            if (parentFolder == null || Boolean.TRUE.equals(parentFolder.getDeleted())) {
                throw new NotFoundException("error.folder.notFoundOrDeleted");
            }
            if (!this.folderShareService.checkCanEdit(SecurityUtil.getCurrentUserFromThreadLocal(), parentFolder)) {
                throw new ForbiddenException("error.forbidden.uploadFolderHere");
            }
        }

        for (String relativePath : folderUploadReq.getRelativePaths()) {
            if (relativePath == null || relativePath.isEmpty()) continue;
            String[] parts = relativePath.split("/");
            String firstFolderName = parts[0];

            if (parentFolder != null) {
                if (folderService.existsByNameAndParentAndIsDeletedFalseAndIdNot(firstFolderName, parentFolder, null)) {
                    throw new UniqueConstraintException("error.unique.folderNameInParent");
                }
            } else {
                if (folderService.existsByNameAndCreatedByAndParentIsNullAndIsDeletedFalseAndIdNot(firstFolderName,
                        SecurityUtil.getCurrentUserFromThreadLocal(), null)) {
                    throw new UniqueConstraintException("error.unique.folderInRoot");
                }
            }
            break;
        }

        Folder uploadedFolder = folderService.uploadNewFolder(parentFolder, folderUploadReq.getFiles(), folderUploadReq.getRelativePaths());
        if (uploadedFolder.getParent() != null) {
            this.folderShareService.handleShareAfterCreate(uploadedFolder.getParent(), uploadedFolder);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedFolder);
    }

    @GetMapping("/secure/folders/download/{id}")
    public ResponseEntity<StreamingResponseBody> downloadFolder(@PathVariable Integer id) {
        Folder rootFolder = folderService.getFolderById(id);
        if (rootFolder == null || Boolean.TRUE.equals(rootFolder.getDeleted())) {
            throw new NotFoundException("error.folder.notFoundOrDeleted");
        }

        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();
        if (!this.folderShareService.checkCanView(currentUser, rootFolder)) {
            throw new ForbiddenException("error.forbidden.downloadFolder");
        }

        StreamingResponseBody stream = outputStream -> {
            try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {
                this.folderService.zipFolderIterative(rootFolder, zipOut);
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + rootFolder.getName() + ".zip\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(stream);
    }

    @PostMapping("/secure/folders/download/multiple")
    public ResponseEntity<StreamingResponseBody> downloadMultiple(@RequestBody List<Integer> folderIds) {
        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();

        List<Folder> folders = folderService.getFoldersByIds(folderIds);
        Map<Integer, Folder> folderMap = folders.stream()
                .filter(f -> !f.getDeleted() && this.folderShareService.checkCanView(currentUser, f))
                .collect(Collectors.toMap(Folder::getId, f -> f));
        List<Integer> notFoundIds = folderIds.stream().filter(id -> !folderMap.containsKey(id)).toList();

        if (!notFoundIds.isEmpty()) {
            throw new NotFoundException("error.folder.notFoundById", notFoundIds);
        }

        StreamingResponseBody stream = outputStream -> {
            try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {
                for (Folder folder : folders) {
                    this.folderService.zipFolderIterative(folder, zipOut);
                }
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"folders.zip\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(stream);
    }

    @PatchMapping(path = "/secure/folders/{id}")
    @ApiMessage(key = "api.folder.update", message = "Update folder")
    public ResponseEntity<FolderDTO> update(@PathVariable Integer id, @RequestBody @Valid Folder request) {
        Folder folder = this.folderService.getFolderById(id);
        if (folder == null || Boolean.TRUE.equals(folder.getDeleted())) {
            throw new NotFoundException("error.folder.notFoundOrDeleted");
        }

        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();
        if (!this.folderShareService.checkCanEdit(currentUser, folder)) {
            throw new ForbiddenException("error.forbidden.editFolder");
        }

        if (folder.getParent() != null) {
            if (folderService.existsByNameAndParentAndIsDeletedFalseAndIdNot(request.getName(), folder.getParent(), folder.getId())) {
                throw new UniqueConstraintException("error.unique.renameInParent");
            }
        } else {
            if (folderService.existsByNameAndCreatedByAndParentIsNullAndIsDeletedFalseAndIdNot(request.getName(), SecurityUtil.getCurrentUserFromThreadLocal(),
                    folder.getId())) {
                throw new UniqueConstraintException("error.unique.renameInRoot");
            }
        }

        folder.setName(request.getName());
        return ResponseEntity.status(HttpStatus.OK).body(this.folderService.convertFolderToFolderDTO(this.folderService.save(folder)));
    }

    @PatchMapping(path = "/secure/folders")
    @ApiMessage(key = "api.folder.trash", message = "Move folder to trash")
    public ResponseEntity<Void> softDelete(@RequestBody List<Integer> ids) {
        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();

        List<Folder> folders = this.folderService.getFoldersByIds(ids);
        Map<Integer, Folder> folderMap = folders.stream()
                .filter(f -> !f.getDeleted() && this.folderService.isOwnerFolder(f, currentUser))
                .collect(Collectors.toMap(f -> f.getId(), f -> f));
        List<Integer> notFoundIds = ids.stream().filter(id -> !folderMap.containsKey(id)).toList();

        if (!notFoundIds.isEmpty()) {
            throw new NotFoundException("error.folder.idsSoftDeleted", notFoundIds);
        }

        for (Folder f : folders) {
            this.folderService.softDeleteFolderAndChildren(f);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping(path = "/secure/folders/restore")
    @ApiMessage(key = "api.folder.restore", message = "Restore folder")
    public ResponseEntity<List<FolderDTO>> restore(@RequestBody List<Integer> ids) {
        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();

        List<Folder> folders = this.folderService.getFoldersByIds(ids);
        Map<Integer, Folder> folderMap = folders.stream()
                .filter(f -> f.getDeleted() && this.folderService.isOwnerFolder(f, currentUser))
                .collect(Collectors.toMap(f -> f.getId(), f -> f));
        List<Integer> notFoundIds = ids.stream().filter(id -> !folderMap.containsKey(id)).toList();

        if (!notFoundIds.isEmpty()) {
            throw new NotFoundException("error.folder.idsNotSoftDeleted", notFoundIds);
        }

        for (Folder f : folders) {
            this.folderService.restoreFolderAndChildren(f);
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(folders.stream().map(f -> this.folderService.convertFolderToFolderDTO(f))
                        .collect(Collectors.toList()));
    }

    @DeleteMapping(path = "/secure/folders/permanent")
    @ApiMessage(key = "api.folder.permanentDelete", message = "Permanently delete folder")
    public ResponseEntity<Void> hardDelete(@RequestBody List<Integer> ids) {
        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();

        List<Folder> folders = this.folderService.getFoldersByIds(ids);
        Map<Integer, Folder> folderMap = folders.stream()
                .filter(f -> f.getDeleted() && this.folderService.isOwnerFolder(f, currentUser))
                .collect(Collectors.toMap(f -> f.getId(), f -> f));
        List<Integer> notFoundIds = ids.stream().filter(id -> !folderMap.containsKey(id)).toList();

        if (!notFoundIds.isEmpty()) {
            throw new NotFoundException("error.folder.idsNotSoftDeleted", notFoundIds);
        }

        for (Folder f : folders) {
            this.folderService.hardDeleteFolderAndChildren(f);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/secure/folders/copy")
    @ApiMessage(key = "api.folder.copy", message = "Copy folder")
    public ResponseEntity<Void> copyFolders(@RequestBody CopyCutReq request) {
        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();

        List<Folder> folders = this.folderService.getFoldersByIds(request.getIds());
        Map<Integer, Folder> folderMap = folders.stream()
                .filter(f -> !f.getDeleted() && this.folderShareService.checkCanView(currentUser, f))
                .collect(Collectors.toMap(f -> f.getId(), f -> f));
        List<Integer> notFoundIds = request.getIds().stream().filter(id -> !folderMap.containsKey(id)).toList();

        if (!notFoundIds.isEmpty()) {
            throw new NotFoundException("error.folder.idsSoftDeleted", notFoundIds);
        }

        Folder targetFolder = null;
        if (request.getTargetFolderId() != null) {
            targetFolder = this.folderService.getFolderById(request.getTargetFolderId());
            if (targetFolder == null || Boolean.TRUE.equals(targetFolder.getDeleted())) {
                throw new NotFoundException("error.folder.notFoundOrDeleted");
            }
        }

        for (Folder folder : folders) {
            if (targetFolder != null
                    && (folder.getId().equals(targetFolder.getId()) || this.folderService.isDescendant(folder, targetFolder))) {
                throw new FileException("error.folder.copyIntoSelf", folder.getName());
            }
            this.folderService.copyFolder(folder, targetFolder);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/secure/folders/move")
    @ApiMessage(key = "api.folder.move", message = "Move folder")
    public ResponseEntity<Void> moveFolders(@RequestBody CopyCutReq request) {
        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();

        List<Folder> folders = this.folderService.getFoldersByIds(request.getIds());
        Map<Integer, Folder> folderMap = folders.stream()
                .filter(f -> !f.getDeleted() && this.folderService.isOwnerFolder(f, currentUser))
                .collect(Collectors.toMap(f -> f.getId(), f -> f));
        List<Integer> notFoundIds = request.getIds().stream().filter(id -> !folderMap.containsKey(id)).toList();

        if (!notFoundIds.isEmpty()) {
            throw new NotFoundException("error.folder.idsSoftDeleted", notFoundIds);
        }

        Folder targetFolder = null;
        if (request.getTargetFolderId() != null) {
            targetFolder = this.folderService.getFolderById(request.getTargetFolderId());
            if (targetFolder == null || Boolean.TRUE.equals(targetFolder.getDeleted())) {
                throw new NotFoundException("error.folder.notFoundOrDeleted");
            }
        }

        for (Folder folder : folders) {
            if (folder.getParent() == null) {
                if (targetFolder == null) continue;
            } else {
                if (folder.getParent().equals(targetFolder)) continue;
            }

            if (targetFolder != null
                    && (folder.getId().equals(targetFolder.getId()) || this.folderService.isDescendant(folder, targetFolder))) {
                throw new FileException("error.folder.moveIntoSelf", folder.getName());
            }
            this.folderService.moveFolder(folder, targetFolder);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/secure/folders/{id}")
    @ApiMessage(key = "api.folder.detail", message = "View folder details")
    public ResponseEntity<FolderDTO> detail(@PathVariable Integer id) {
        Folder folder = this.folderService.getFolderById(id);
        if (folder == null || Boolean.TRUE.equals(folder.getDeleted())) {
            throw new NotFoundException("error.folder.notFoundOrDeleted");
        }

        if (!this.folderShareService.checkCanView(SecurityUtil.getCurrentUserFromThreadLocal(), folder)) {
            throw new ForbiddenException("error.forbidden.viewFolder");
        }

        return ResponseEntity.status(HttpStatus.OK).body(this.folderService.convertFolderToFolderDTO(folder));
    }

}