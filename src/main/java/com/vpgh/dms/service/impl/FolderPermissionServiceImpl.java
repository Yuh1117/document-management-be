package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.constant.PermissionType;
import com.vpgh.dms.model.entity.Folder;
import com.vpgh.dms.model.entity.FolderPermission;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.model.entity.UserGroup;
import com.vpgh.dms.repository.FolderPermissionRepository;
import com.vpgh.dms.service.FolderPermissionService;
import com.vpgh.dms.service.UserGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class FolderPermissionServiceImpl implements FolderPermissionService {
    @Autowired
    private FolderPermissionRepository folderPermissionRepository;
    @Autowired
    private UserGroupService userGroupService;

    @Override
    public boolean checkCanEdit(User user, Folder folder) {
        if (Objects.equals(folder.getCreatedBy().getId(), user.getId()))
            return true;

        boolean hasPermission = checkUserOrGroupPermission(user, folder, "EDIT");
        if (hasPermission)
            return true;

        return false;
    }

    @Override
    public boolean checkUserOrGroupPermission(User user, Folder folder, String permission) {
        Optional<FolderPermission> userPermission = this.folderPermissionRepository
                .findByFolderAndUserAndPermissionType(folder, user, PermissionType.EDIT);

        if (userPermission.isPresent()) {
            return true;
        }

        List<UserGroup> userGroups = this.userGroupService.getGroupsByUser(user);
        if (!userGroups.isEmpty()) {
            Optional<FolderPermission> groupPermission = this.folderPermissionRepository
                    .findByFolderAndGroupInAndPermissionType(folder, userGroups, PermissionType.EDIT);

            if (groupPermission.isPresent()) {
                return true;
            }
        }

        return false;
    }
}
