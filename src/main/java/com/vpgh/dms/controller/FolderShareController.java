package com.vpgh.dms.controller;

import com.vpgh.dms.model.dto.request.ShareReq;
import com.vpgh.dms.model.entity.Folder;
import com.vpgh.dms.model.entity.FolderShare;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.service.FolderService;
import com.vpgh.dms.service.FolderShareService;
import com.vpgh.dms.service.UserService;
import com.vpgh.dms.util.SecurityUtil;
import com.vpgh.dms.util.annotation.ApiMessage;
import com.vpgh.dms.util.exception.ForbiddenException;
import com.vpgh.dms.util.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class FolderShareController {
    private final FolderService folderService;
    private final FolderShareService folderShareService;
    private final UserService userService;

    public FolderShareController(FolderService folderService, FolderShareService folderShareService, UserService userService) {
        this.folderService = folderService;
        this.folderShareService = folderShareService;
        this.userService = userService;
    }

    @PostMapping("/secure/folders/share")
    @ApiMessage(message = "Tạo mới chia sẻ folder")
    public ResponseEntity<List<FolderShare>> share(@RequestBody ShareReq shareReq) {
        Folder folder = folderService.getFolderById(shareReq.getFolderId());

        if (!folderShareService.checkCanEdit(SecurityUtil.getCurrentUserFromThreadLocal(), folder)) {
            throw new ForbiddenException("Bạn không có quyền chia sẻ thư mục này");
        }

        List<FolderShare> res = folderShareService.shareFolder(folder, shareReq.getShares());
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/secure/folders/share/{id}")
    @ApiMessage(message = "Xem chi tiết chia sẻ folder")
    public ResponseEntity<List<FolderShare>> getShare(@PathVariable Integer id) {
        Folder folder = folderService.getFolderById(id);
        if (folder == null || Boolean.TRUE.equals(folder.getDeleted())) {
            throw new NotFoundException("Thư mục không tồn tại hoặc đã bị xóa.");
        }

        if (!folderShareService.checkCanView(SecurityUtil.getCurrentUserFromThreadLocal(), folder)) {
            throw new ForbiddenException("Bạn không có quyền xem.");
        }

        return ResponseEntity.ok(folderShareService.getShares(folder));
    }

    @DeleteMapping("/secure/folders/share/{id}")
    @ApiMessage(message = "Xóa quyền chia sẻ folder")
    public ResponseEntity<Void> unshare(@PathVariable Integer id, @RequestBody List<Integer> request) {
        Folder folder = folderService.getFolderById(id);
        if (folder == null || Boolean.TRUE.equals(folder.getDeleted())) {
            throw new NotFoundException("Thư mục không tồn tại hoặc đã bị xóa.");
        }

        if (!folderShareService.checkCanEdit(SecurityUtil.getCurrentUserFromThreadLocal(), folder)) {
            throw new ForbiddenException("Bạn không có quyền chỉnh sửa chia sẻ.");
        }

        List<User> users = userService.getAllByIds(request);
        if (users.isEmpty()) {
            throw new NotFoundException("Không tìm thấy người dùng.");
        }

        folderShareService.removeShares(folder, users);
        return ResponseEntity.noContent().build();
    }
}