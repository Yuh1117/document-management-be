package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.constant.ShareType;
import com.vpgh.dms.model.dto.request.ShareReq;
import com.vpgh.dms.model.entity.Folder;
import com.vpgh.dms.model.entity.FolderShare;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.model.entity.UserGroup;
import com.vpgh.dms.repository.FolderShareRepository;
import com.vpgh.dms.repository.UserRepository;
import com.vpgh.dms.service.FolderService;
import com.vpgh.dms.service.FolderShareService;
import com.vpgh.dms.service.UserGroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FolderShareServiceImpl implements FolderShareService {
    private final FolderShareRepository folderShareRepository;
    private final UserGroupService userGroupService;
    private final FolderService folderService;
    private final UserRepository userRepository;


    public FolderShareServiceImpl(FolderShareRepository folderShareRepository, UserGroupService userGroupService,
                                  FolderService folderService, UserRepository userRepository) {
        this.folderShareRepository = folderShareRepository;
        this.userGroupService = userGroupService;
        this.folderService = folderService;
        this.userRepository = userRepository;
    }

    @Override
    public boolean checkCanView(User user, Folder folder) {
        return hasFolderPermission(user, folder, ShareType.VIEW);
    }

    @Override
    public boolean checkCanEdit(User user, Folder folder) {
        return hasFolderPermission(user, folder, ShareType.EDIT);
    }

    @Override
    public boolean hasFolderPermission(User user, Folder folder, ShareType required) {
        if (this.folderService.isOwnerFolder(folder, user)) {
            return true;
        }

        if (required == ShareType.VIEW) {
            if (checkUserOrGroupPermission(user, folder, ShareType.VIEW) || checkUserOrGroupPermission(user, folder, ShareType.EDIT)) {
                return true;
            }
        } else {
            if (checkUserOrGroupPermission(user, folder, ShareType.EDIT)) {
                return true;
            }
        }

        if (Boolean.TRUE.equals(folder.getInheritPermissions()) && folder.getParent() != null) {
            return hasFolderPermission(user, folder.getParent(), required);
        }

        return false;
    }

    @Override
    public List<FolderShare> shareFolder(Folder folder, List<ShareReq.UserShareDTO> userShareDTOS) {
        List<FolderShare> shares = new ArrayList<>();

        for (ShareReq.UserShareDTO dto : userShareDTOS) {
            User user = this.userRepository.findByEmail(dto.getEmail());
            if (this.folderService.isOwnerFolder(folder, user)) continue;

            FolderShare existing = this.folderShareRepository.findByFolderAndUser(folder, user).orElse(null);
            if (existing != null) {
                existing.setShareType(dto.getShareType());
                shares.add(existing);
            } else {
                FolderShare share = new FolderShare();
                share.setFolder(folder);
                share.setUser(user);
                share.setShareType(dto.getShareType());
                shares.add(share);
            }
        }

        return folderShareRepository.saveAll(shares);
    }

    @Override
    public List<FolderShare> getShares(Folder folder) {
        return folderShareRepository.findByFolder(folder);
    }

    @Override
    @Transactional
    public void removeShares(Folder folder, List<User> users) {
        folderShareRepository.deleteByFolderAndUserIn(folder, users);
    }

    private boolean checkUserOrGroupPermission(User user, Folder folder, ShareType permission) {
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
