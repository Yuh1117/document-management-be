package com.vpgh.dms.controller;

import com.vpgh.dms.model.entity.Folder;
import com.vpgh.dms.service.FolderService;
import com.vpgh.dms.util.SecurityUtil;
import com.vpgh.dms.util.annotation.ApiMessage;
import com.vpgh.dms.util.exception.NotFoundException;
import com.vpgh.dms.util.exception.UniqueConstraintException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class FolderController {

    @Autowired
    private FolderService folderService;

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
            if (folderService.existsByNameAndCreatedByAndParentIsNullAndIsDeletedFalseAndIdNot(folder.getName(), SecurityUtil.getCurrentUserFromThreadLocal(),
                    folder.getId())) {
                throw new UniqueConstraintException("Không thể tạo thư mục gốc với tên này.");
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(this.folderService.save(folder));
    }

    @PatchMapping(path = "/secure/folders/{id}")
    @ApiMessage(message = "Cập nhật thư mục")
    public ResponseEntity<Folder> update(@PathVariable Integer id, @RequestBody @Valid Folder request) {
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
        return ResponseEntity.status(HttpStatus.OK).body(this.folderService.save(folder));
    }

    @DeleteMapping(path = "/secure/folders/{id}")
    @ApiMessage(message = "Chuyển thư mục vào thùng rác")
    public ResponseEntity<Void> softDelete(@PathVariable Integer id) {
        Folder folder = this.folderService.getFolderById(id);
        if (folder == null || Boolean.TRUE.equals(folder.getDeleted())) {
            throw new NotFoundException("Thư mục không tồn tại hoặc đã bị xóa");
        }

        this.folderService.softDeleteFolderAndChildren(folder);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping(path = "/secure/folders/restore/{id}")
    @ApiMessage(message = "Khôi phục thư mục")
    public ResponseEntity<Folder> restore(@PathVariable Integer id) {
        Folder folder = this.folderService.getFolderById(id);
        if (folder == null || !Boolean.TRUE.equals(folder.getDeleted())) {
            throw new NotFoundException("Thư mục không tồn tại hoặc chưa bị xoá mềm");
        }

        this.folderService.restoreFolderAndChildren(folder);
        return ResponseEntity.status(HttpStatus.OK).body(folder);
    }

    @DeleteMapping(path = "/secure/folders/permanent/{id}")
    @ApiMessage(message = "Xoá vĩnh viễn thư mục")
    public ResponseEntity<Void> hardDelete(@PathVariable Integer id) {
        Folder folder = this.folderService.getFolderById(id);
        if (folder == null || !Boolean.TRUE.equals(folder.getDeleted())) {
            throw new NotFoundException("Thư mục không tồn tại hoặc chưa bị xoá mềm");
        }

        this.folderService.hardDeleteFolderAndChildren(folder);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
