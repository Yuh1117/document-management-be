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
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
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

    public FolderShareController(FolderService folderService, FolderShareService folderShareService,
            UserService userService) {
        this.folderService = folderService;
        this.folderShareService = folderShareService;
        this.userService = userService;
    }

    @PostMapping("/secure/folders/share")
    @ApiMessage(key = "api.folderShare.create", message = "Create folder share")
    public ResponseEntity<List<FolderShare>> share(@Valid @RequestBody ShareReq shareReq) throws MessagingException {
        Folder folder = resolveEditableFolder(shareReq.getFolderId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(folderShareService.shareFolder(folder, shareReq.getShares()));
    }

    @GetMapping("/secure/folders/share/{id}")
    @ApiMessage(key = "api.folderShare.detail", message = "View folder share details")
    public ResponseEntity<List<FolderShare>> getShare(@PathVariable Integer id) {
        Folder folder = resolveViewableFolder(id);
        return ResponseEntity.ok(folderShareService.getShares(folder));
    }

    @DeleteMapping("/secure/folders/share/{id}")
    @ApiMessage(key = "api.folderShare.delete", message = "Remove folder share permission")
    public ResponseEntity<Void> unshare(@PathVariable Integer id, @RequestBody List<Integer> request) {
        Folder folder = resolveEditableFolder(id);

        List<User> users = userService.getAllByIds(request);
        if (users.isEmpty()) {
            throw new NotFoundException("error.user.notFoundWithDot");
        }

        folderShareService.removeShares(folder, users);
        return ResponseEntity.noContent().build();
    }

    private Folder resolveEditableFolder(Integer id) {
        Folder folder = folderService.getFolderById(id);
        if (folder == null || Boolean.TRUE.equals(folder.getDeleted())) {
            throw new NotFoundException("error.folder.notFoundOrDeleted");
        }
        if (!folderShareService.checkCanEdit(SecurityUtil.getCurrentUserFromThreadLocal(), folder)) {
            throw new ForbiddenException("error.forbidden.editFolder");
        }
        return folder;
    }

    private Folder resolveViewableFolder(Integer id) {
        Folder folder = folderService.getFolderById(id);
        if (folder == null || Boolean.TRUE.equals(folder.getDeleted())) {
            throw new NotFoundException("error.folder.notFoundOrDeleted");
        }
        if (!folderShareService.checkCanView(SecurityUtil.getCurrentUserFromThreadLocal(), folder)) {
            throw new ForbiddenException("error.forbidden.viewFolder");
        }
        return folder;
    }
}