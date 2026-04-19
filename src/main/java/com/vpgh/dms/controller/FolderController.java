package com.vpgh.dms.controller;

import com.vpgh.dms.model.dto.FolderDTO;
import com.vpgh.dms.model.dto.request.CopyCutReq;
import com.vpgh.dms.model.dto.request.FolderUploadReq;
import com.vpgh.dms.model.dto.response.FolderUploadPlan;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api")
public class FolderController {
    private final FolderService folderService;
    private final FolderShareService folderShareService;
    private final DocumentService documentService;
    private final Executor uploadExecutor;

    public FolderController(FolderService folderService, FolderShareService folderShareService,
            DocumentService documentService, @Qualifier("uploadExecutor") Executor uploadExecutor) {
        this.folderService = folderService;
        this.folderShareService = folderShareService;
        this.documentService = documentService;
        this.uploadExecutor = uploadExecutor;
    }

    @PostMapping(path = "/secure/folders")
    @ApiMessage(key = "api.folder.create", message = "Create folder")
    public ResponseEntity<Folder> create(@RequestBody @Valid Folder folder) {
        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();

        if (folder.getParent() != null && folder.getParent().getId() != null) {
            Folder parent = resolveEditableFolder(folder.getParent().getId(), currentUser);
            folder.setParent(parent);
        }

        if (folder.getParent() != null) {
            if (folderService.existsByNameAndParentAndIsDeletedFalseAndIdNot(folder.getName(), folder.getParent(),
                    folder.getId())) {
                throw new UniqueConstraintException("error.unique.folderNameInParent");
            }
        } else {
            if (folderService.existsByNameAndCreatedByAndParentIsNullAndIsDeletedFalseAndIdNot(folder.getName(),
                    currentUser, folder.getId())) {
                throw new UniqueConstraintException("error.unique.folderInRoot");
            }
        }

        Folder saved = this.folderService.save(folder);
        if (saved.getParent() != null) {
            this.folderShareService.handleShareAfterCreate(saved.getParent(), saved);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping(path = "/secure/folders/upload")
    @ApiMessage(key = "api.folder.upload", message = "Upload folder")
    public ResponseEntity<Folder> uploadFolder(@Valid @ModelAttribute FolderUploadReq folderUploadReq)
            throws IOException {
        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();
        Folder parentFolder = resolveEditableFolder(folderUploadReq.getParentId(), currentUser);

        for (String relativePath : folderUploadReq.getRelativePaths()) {
            if (relativePath == null || relativePath.isEmpty())
                continue;
            String firstFolderName = relativePath.split("/")[0];

            if (parentFolder != null) {
                if (folderService.existsByNameAndParentAndIsDeletedFalseAndIdNot(firstFolderName, parentFolder, null)) {
                    throw new UniqueConstraintException("error.unique.folderNameInParent");
                }
            } else {
                if (folderService.existsByNameAndCreatedByAndParentIsNullAndIsDeletedFalseAndIdNot(firstFolderName,
                        currentUser, null)) {
                    throw new UniqueConstraintException("error.unique.folderInRoot");
                }
            }
            break;
        }

        List<MultipartFile> files = folderUploadReq.getFiles();
        FolderUploadPlan plan = folderService.buildFolderStructure(parentFolder, folderUploadReq.getRelativePaths());

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            Folder targetFolder = plan.targetFolders().get(i);
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    documentService.uploadNewFile(file, targetFolder);
                } catch (IOException e) {
                    throw new CompletionException(e);
                }
            }, uploadExecutor));
        }

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof IOException io)
                throw io;
            throw e;
        }

        Folder uploadedFolder = plan.rootFolder();
        if (uploadedFolder != null && uploadedFolder.getParent() != null) {
            this.folderShareService.handleShareAfterCreate(uploadedFolder.getParent(), uploadedFolder);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedFolder);
    }

    @GetMapping("/secure/folders/download/{id}")
    public ResponseEntity<StreamingResponseBody> downloadFolder(@PathVariable Integer id) {
        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();
        Folder rootFolder = resolveViewableFolder(id, currentUser);

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
        List<Folder> folders = resolveViewableFolders(folderIds, currentUser);

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
        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();
        Folder folder = resolveEditableFolder(id, currentUser);

        if (folder.getParent() != null) {
            if (folderService.existsByNameAndParentAndIsDeletedFalseAndIdNot(request.getName(), folder.getParent(),
                    folder.getId())) {
                throw new UniqueConstraintException("error.unique.renameInParent");
            }
        } else {
            if (folderService.existsByNameAndCreatedByAndParentIsNullAndIsDeletedFalseAndIdNot(request.getName(),
                    currentUser, folder.getId())) {
                throw new UniqueConstraintException("error.unique.renameInRoot");
            }
        }

        folder.setName(request.getName());
        return ResponseEntity.ok(this.folderService.convertFolderToFolderDTO(this.folderService.save(folder)));
    }

    @PatchMapping(path = "/secure/folders")
    @ApiMessage(key = "api.folder.trash", message = "Move folder to trash")
    public ResponseEntity<Void> softDelete(@RequestBody List<Integer> ids) {
        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();
        List<Folder> folders = resolveOwnedActiveFolders(ids, currentUser);

        for (Folder f : folders) {
            this.folderService.softDeleteFolderAndChildren(f);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping(path = "/secure/folders/restore")
    @ApiMessage(key = "api.folder.restore", message = "Restore folder")
    public ResponseEntity<List<FolderDTO>> restore(@RequestBody List<Integer> ids) {
        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();
        List<Folder> folders = resolveOwnedTrashedFolders(ids, currentUser);

        for (Folder f : folders) {
            this.folderService.restoreFolderAndChildren(f);
        }
        return ResponseEntity
                .ok(folders.stream().map(folderService::convertFolderToFolderDTO).collect(Collectors.toList()));
    }

    @DeleteMapping(path = "/secure/folders/permanent")
    @ApiMessage(key = "api.folder.permanentDelete", message = "Permanently delete folder")
    public ResponseEntity<Void> hardDelete(@RequestBody List<Integer> ids) {
        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();
        List<Folder> folders = resolveOwnedTrashedFolders(ids, currentUser);

        for (Folder f : folders) {
            this.folderService.hardDeleteFolderAndChildren(f);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/secure/folders/copy")
    @ApiMessage(key = "api.folder.copy", message = "Copy folder")
    public ResponseEntity<Void> copyFolders(@RequestBody CopyCutReq request) {
        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();
        List<Folder> folders = resolveViewableFolders(request.getIds(), currentUser);
        Folder targetFolder = resolveTargetFolder(request.getTargetFolderId());

        for (Folder folder : folders) {
            if (targetFolder != null
                    && (folder.getId().equals(targetFolder.getId())
                            || this.folderService.isDescendant(folder, targetFolder))) {
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
        List<Folder> folders = resolveOwnedActiveFolders(request.getIds(), currentUser);
        Folder targetFolder = resolveTargetFolder(request.getTargetFolderId());

        for (Folder folder : folders) {
            if (folder.getParent() == null) {
                if (targetFolder == null)
                    continue;
            } else {
                if (folder.getParent().equals(targetFolder))
                    continue;
            }
            if (targetFolder != null
                    && (folder.getId().equals(targetFolder.getId())
                            || this.folderService.isDescendant(folder, targetFolder))) {
                throw new FileException("error.folder.moveIntoSelf", folder.getName());
            }
            this.folderService.moveFolder(folder, targetFolder);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/secure/folders/{id}")
    @ApiMessage(key = "api.folder.detail", message = "View folder details")
    public ResponseEntity<FolderDTO> detail(@PathVariable Integer id) {
        Folder folder = resolveViewableFolder(id, SecurityUtil.getCurrentUserFromThreadLocal());
        return ResponseEntity.ok(this.folderService.convertFolderToFolderDTO(folder));
    }

    private Folder resolveEditableFolder(Integer folderId, User user) {
        if (folderId == null)
            return null;
        Folder folder = folderService.getFolderById(folderId);
        if (folder == null || Boolean.TRUE.equals(folder.getDeleted())) {
            throw new NotFoundException("error.folder.notFoundOrDeleted");
        }
        if (!folderShareService.checkCanEdit(user, folder)) {
            throw new ForbiddenException("error.forbidden.editFolder");
        }
        return folder;
    }

    private Folder resolveViewableFolder(Integer folderId, User user) {
        Folder folder = folderService.getFolderById(folderId);
        if (folder == null || Boolean.TRUE.equals(folder.getDeleted())) {
            throw new NotFoundException("error.folder.notFoundOrDeleted");
        }
        if (!folderShareService.checkCanView(user, folder)) {
            throw new ForbiddenException("error.forbidden.viewFolder");
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

    private List<Folder> resolveViewableFolders(List<Integer> ids, User user) {
        List<Folder> folders = folderService.getFoldersByIds(ids);
        Map<Integer, Folder> folderMap = folders.stream()
                .filter(f -> !f.getDeleted() && folderShareService.checkCanView(user, f))
                .collect(Collectors.toMap(Folder::getId, f -> f));
        List<Integer> notFoundIds = ids.stream().filter(id -> !folderMap.containsKey(id)).toList();
        if (!notFoundIds.isEmpty()) {
            throw new NotFoundException("error.folder.idsSoftDeleted", notFoundIds);
        }
        return folders;
    }

    private List<Folder> resolveOwnedActiveFolders(List<Integer> ids, User user) {
        List<Folder> folders = folderService.getFoldersByIds(ids);
        Map<Integer, Folder> folderMap = folders.stream()
                .filter(f -> !f.getDeleted() && folderService.isOwnerFolder(f, user))
                .collect(Collectors.toMap(Folder::getId, f -> f));
        List<Integer> notFoundIds = ids.stream().filter(id -> !folderMap.containsKey(id)).toList();
        if (!notFoundIds.isEmpty()) {
            throw new NotFoundException("error.folder.idsSoftDeleted", notFoundIds);
        }
        return folders;
    }

    private List<Folder> resolveOwnedTrashedFolders(List<Integer> ids, User user) {
        List<Folder> folders = folderService.getFoldersByIds(ids);
        Map<Integer, Folder> folderMap = folders.stream()
                .filter(f -> f.getDeleted() && folderService.isOwnerFolder(f, user))
                .collect(Collectors.toMap(Folder::getId, f -> f));
        List<Integer> notFoundIds = ids.stream().filter(id -> !folderMap.containsKey(id)).toList();
        if (!notFoundIds.isEmpty()) {
            throw new NotFoundException("error.folder.idsNotSoftDeleted", notFoundIds);
        }
        return folders;
    }

}