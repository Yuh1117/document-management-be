package com.vpgh.dms.controller;

import com.vpgh.dms.model.dto.FolderDTO;
import com.vpgh.dms.model.dto.request.CopyCutReq;
import com.vpgh.dms.model.dto.request.FolderUploadReq;
import com.vpgh.dms.model.entity.Folder;
import com.vpgh.dms.service.DocumentService;
import com.vpgh.dms.service.FolderShareService;
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
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api")
public class FolderController {

    @Autowired
    private FolderService folderService;
    @Autowired
    private FolderShareService folderShareService;
    @Autowired
    private DocumentService documentService;

    @PostMapping(path = "/secure/folders")
    @ApiMessage(message = "Tạo mới thư mục")
    public ResponseEntity<Folder> create(@RequestBody @Valid Folder folder) {
        if (folder.getParent() != null && folder.getParent().getId() != null) {
            Folder f = this.folderService.getFolderById(folder.getParent().getId());
            if (f == null || Boolean.TRUE.equals(folder.getDeleted())) {
                throw new NotFoundException("Thư mục không tồn tại hoặc đã bị xóa");
            }
            folder.setParent(f);
        }

        if (folder.getParent() != null) {
            if (folderService.existsByNameAndParentAndIsDeletedFalseAndIdNot(folder.getName(), folder.getParent(), folder.getId())) {
                throw new UniqueConstraintException("Không thể tạo thư mục với tên này trong cùng thư mục cha.");
            }
        } else {
            if (folderService.existsByNameAndCreatedByAndParentIsNullAndIsDeletedFalseAndIdNot(folder.getName(),
                    SecurityUtil.getCurrentUserFromThreadLocal(), folder.getId())) {
                throw new UniqueConstraintException("Không thể tạo thư mục này trong thư mục gốc.");
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(this.folderService.save(folder));
    }

    @PostMapping(path = "/secure/folders/upload")
    @ApiMessage(message = "Upload thư mục")
    public ResponseEntity<Folder> uploadFolder(@Valid @ModelAttribute FolderUploadReq folderUploadReq) throws IOException {
        Folder parentFolder = null;
        if (folderUploadReq.getParentId() != null) {
            parentFolder = this.folderService.getFolderById(folderUploadReq.getParentId());
            if (parentFolder == null || Boolean.TRUE.equals(parentFolder.getDeleted())) {
                throw new NotFoundException("Thư mục không tồn tại hoặc đã bị xóa");
            }
        }

        for (String relativePath : folderUploadReq.getRelativePaths()) {
            if (relativePath == null || relativePath.isEmpty()) continue;
            String[] parts = relativePath.split("/");
            String firstFolderName = parts[0];

            if (parentFolder != null) {
                if (folderService.existsByNameAndParentAndIsDeletedFalseAndIdNot(firstFolderName, parentFolder, null)) {
                    throw new UniqueConstraintException("Không thể tạo thư mục với tên này trong cùng thư mục cha.");
                }
            } else {
                if (folderService.existsByNameAndCreatedByAndParentIsNullAndIsDeletedFalseAndIdNot(firstFolderName,
                        SecurityUtil.getCurrentUserFromThreadLocal(), null)) {
                    throw new UniqueConstraintException("Không thể tạo thư mục này trong thư mục gốc.");
                }
            }
        }

        Folder uploadedFolder = folderService.uploadNewFolder(parentFolder, folderUploadReq.getFiles(), folderUploadReq.getRelativePaths());
        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedFolder);
    }

    @GetMapping("/secure/folders/download/{id}")
    public ResponseEntity<StreamingResponseBody> downloadFolder(@PathVariable Integer id) {
        Folder rootFolder = folderService.getFolderById(id);
        if (rootFolder == null || Boolean.TRUE.equals(rootFolder.getDeleted())) {
            throw new NotFoundException("Thư mục không tồn tại hoặc đã bị xóa");
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
        List<Folder> folders = folderService.getFoldersByIds(folderIds);
        Map<Integer, Folder> folderMap = folders.stream()
                .filter(f -> !f.getDeleted())
                .collect(Collectors.toMap(Folder::getId, f -> f));
        List<Integer> notFoundIds = folderIds.stream().filter(id -> !folderMap.containsKey(id)).toList();

        if (!notFoundIds.isEmpty()) {
            throw new NotFoundException("Không tìm thấy thư mục với ID: " + notFoundIds);
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
    @ApiMessage(message = "Cập nhật thư mục")
    public ResponseEntity<FolderDTO> update(@PathVariable Integer id, @RequestBody @Valid Folder request) {
        Folder folder = this.folderService.getFolderById(id);
        if (folder == null || Boolean.TRUE.equals(folder.getDeleted())) {
            throw new NotFoundException("Thư mục không tồn tại hoặc đã bị xóa");
        }

        if (folder.getParent() != null) {
            if (folderService.existsByNameAndParentAndIsDeletedFalseAndIdNot(request.getName(), folder.getParent(), folder.getId())) {
                throw new UniqueConstraintException("Không thể đổi tên này trong cùng thư mục cha.");
            }
        } else {
            if (folderService.existsByNameAndCreatedByAndParentIsNullAndIsDeletedFalseAndIdNot(request.getName(), SecurityUtil.getCurrentUserFromThreadLocal(),
                    folder.getId())) {
                throw new UniqueConstraintException("Không thể đổi tên này trong thư mục gốc");
            }
        }

        folder.setName(request.getName());
        return ResponseEntity.status(HttpStatus.OK).body(this.folderService.convertFolderToFolderDTO(this.folderService.save(folder)));
    }

    @PatchMapping(path = "/secure/folders")
    @ApiMessage(message = "Chuyển thư mục vào thùng rác")
    public ResponseEntity<Void> softDelete(@RequestBody List<Integer> ids) {
        List<Folder> folders = this.folderService.getFoldersByIds(ids);
        Map<Integer, Folder> folderMap = folders.stream()
                .filter(f -> !f.getDeleted())
                .collect(Collectors.toMap(f -> f.getId(), f -> f));
        List<Integer> notFoundIds = ids.stream().filter(id -> !folderMap.containsKey(id)).toList();

        if (!notFoundIds.isEmpty()) {
            throw new NotFoundException("Không tìm thấy thư mục với các ID (hoặc đã bị xóa mềm): " + notFoundIds);
        }

        for (Folder f : folders) {
            this.folderService.softDeleteFolderAndChildren(f);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping(path = "/secure/folders/restore")
    @ApiMessage(message = "Khôi phục thư mục")
    public ResponseEntity<List<FolderDTO>> restore(@RequestBody List<Integer> ids) {
        List<Folder> folders = this.folderService.getFoldersByIds(ids);
        Map<Integer, Folder> folderMap = folders.stream()
                .filter(f -> f.getDeleted())
                .collect(Collectors.toMap(f -> f.getId(), f -> f));
        List<Integer> notFoundIds = ids.stream().filter(id -> !folderMap.containsKey(id)).toList();

        if (!notFoundIds.isEmpty()) {
            throw new NotFoundException("Không tìm thấy thư mục với các ID (hoặc chưa bị xóa mềm): " + notFoundIds);
        }

        for (Folder f : folders) {
            this.folderService.restoreFolderAndChildren(f);
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(folders.stream().map(f -> this.folderService.convertFolderToFolderDTO(f))
                        .collect(Collectors.toList()));
    }

    @DeleteMapping(path = "/secure/folders/permanent")
    @ApiMessage(message = "Xoá vĩnh viễn thư mục")
    public ResponseEntity<Void> hardDelete(@RequestBody List<Integer> ids) {
        List<Folder> folders = this.folderService.getFoldersByIds(ids);
        Map<Integer, Folder> folderMap = folders.stream()
                .filter(f -> f.getDeleted())
                .collect(Collectors.toMap(f -> f.getId(), f -> f));
        List<Integer> notFoundIds = ids.stream().filter(id -> !folderMap.containsKey(id)).toList();

        if (!notFoundIds.isEmpty()) {
            throw new NotFoundException("Không tìm thấy thư mục với các ID (hoặc chưa bị xóa mềm): " + notFoundIds);
        }

        for (Folder f : folders) {
            this.folderService.hardDeleteFolderAndChildren(f);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/secure/folders/copy")
    @ApiMessage(message = "Sao chép thư mục")
    public ResponseEntity<Void> copyFolders(@RequestBody CopyCutReq request) {
        List<Folder> folders = this.folderService.getFoldersByIds(request.getIds());
        Map<Integer, Folder> folderMap = folders.stream()
                .filter(f -> !f.getDeleted())
                .collect(Collectors.toMap(f -> f.getId(), f -> f));
        List<Integer> notFoundIds = request.getIds().stream().filter(id -> !folderMap.containsKey(id)).toList();

        if (!notFoundIds.isEmpty()) {
            throw new NotFoundException("Không tìm thấy thư mục với các ID (hoặc đã bị xóa mềm): " + notFoundIds);
        }

        Folder targetFolder = null;
        if (request.getTargetFolderId() != null) {
            targetFolder = this.folderService.getFolderById(request.getTargetFolderId());
            if (targetFolder == null || Boolean.TRUE.equals(targetFolder.getDeleted())) {
                throw new NotFoundException("Thư mục không tồn tại hoặc đã bị xóa");
            }
        }

        for (Folder folder : folders) {
            this.folderService.copyFolder(folder, targetFolder);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/secure/folders/move")
    @ApiMessage(message = "Di chuyển thư mục")
    public ResponseEntity<Void> moveFolders(@RequestBody CopyCutReq request) {
        List<Folder> folders = this.folderService.getFoldersByIds(request.getIds());
        Map<Integer, Folder> folderMap = folders.stream()
                .filter(f -> !f.getDeleted())
                .collect(Collectors.toMap(f -> f.getId(), f -> f));
        List<Integer> notFoundIds = request.getIds().stream().filter(id -> !folderMap.containsKey(id)).toList();

        if (!notFoundIds.isEmpty()) {
            throw new NotFoundException("Không tìm thấy thư mục với các ID (hoặc đã bị xóa mềm): " + notFoundIds);
        }

        Folder targetFolder = null;
        if (request.getTargetFolderId() != null) {
            targetFolder = this.folderService.getFolderById(request.getTargetFolderId());
            if (targetFolder == null || Boolean.TRUE.equals(targetFolder.getDeleted())) {
                throw new NotFoundException("Thư mục không tồn tại hoặc đã bị xóa");
            }
        }

        for (Folder folder : folders) {
            this.folderService.moveFolder(folder, targetFolder);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/secure/folders/{id}")
    @ApiMessage(message = "Xem thư mục")
    public ResponseEntity<FolderDTO> detail(@PathVariable Integer id) {
        Folder folder = this.folderService.getFolderById(id);
        if (folder == null || Boolean.TRUE.equals(folder.getDeleted())) {
            throw new NotFoundException("Thư mục không tồn tại hoặc đã bị xóa");
        }

        if (!this.folderShareService.checkCanView(SecurityUtil.getCurrentUserFromThreadLocal(), folder)) {
            throw new ForbiddenException("Bạn không có quyền xem thư mục này");
        }

        return ResponseEntity.status(HttpStatus.OK).body(this.folderService.convertFolderToFolderDTO(folder));
    }

}
