package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.constant.ShareType;
import com.vpgh.dms.model.entity.Folder;
import com.vpgh.dms.model.entity.FolderShare;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.model.entity.UserGroup;
import com.vpgh.dms.repository.FolderShareRepository;
import com.vpgh.dms.service.FolderShareService;
import com.vpgh.dms.service.UserGroupService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class FolderShareServiceImpl implements FolderShareService {
    private final FolderShareRepository folderShareRepository;
    private final UserGroupService userGroupService;

    public FolderShareServiceImpl(FolderShareRepository folderShareRepository, UserGroupService userGroupService) {
        this.folderShareRepository = folderShareRepository;
        this.userGroupService = userGroupService;
    }

    @Override
    public boolean checkCanView(User user, Folder folder) {
        if (Objects.equals(folder.getCreatedBy().getId(), user.getId()))
            return true;

        boolean hasPermission = checkUserOrGroupPermission(user, folder, ShareType.VIEW);
        if (hasPermission)
            return true;

        return false;
    }

    @Override
    public boolean checkCanEdit(User user, Folder folder) {
        if (Objects.equals(folder.getCreatedBy().getId(), user.getId()))
            return true;

        boolean hasPermission = checkUserOrGroupPermission(user, folder, ShareType.EDIT);
        if (hasPermission)
            return true;

        return false;
    }

    @Override
    public boolean checkUserOrGroupPermission(User user, Folder folder, ShareType permission) {
        Optional<FolderShare> userPermission = this.folderShareRepository
                .findByFolderAndUserAndShareType(folder, user, permission);

        if (userPermission.isPresent()) {
            return true;
        }

        List<UserGroup> userGroups = this.userGroupService.getGroupsByUser(user);
        if (!userGroups.isEmpty()) {
            Optional<FolderShare> groupPermission = this.folderShareRepository
                    .findByFolderAndGroupInAndShareType(folder, userGroups, permission);

            if (groupPermission.isPresent()) {
                return true;
            }
        }

        return false;
    }
}
