package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.constant.PermissionType;
import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.DocumentPermission;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.model.entity.UserGroup;
import com.vpgh.dms.repository.DocumentPermissionRepository;
import com.vpgh.dms.service.DocumentPermissionService;
import com.vpgh.dms.service.UserGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class DocumentPermissionServiceImpl implements DocumentPermissionService {
    @Autowired
    private DocumentPermissionRepository documentPermissionRepository;
    @Autowired
    private UserGroupService userGroupService;

    @Override
    public boolean checkCanView(User user, Document doc) {
        if (Objects.equals(doc.getCreatedBy().getId(), user.getId()))
            return true;

        boolean hasPermission = checkUserOrGroupPermission(user, doc, PermissionType.VIEW);
        if (hasPermission)
            return true;

        return false;
    }

    @Override
    public boolean checkCanEdit(User user, Document doc) {
        if (Objects.equals(doc.getCreatedBy().getId(), user.getId()))
            return true;

        boolean hasPermission = checkUserOrGroupPermission(user, doc, PermissionType.EDIT);
        if (hasPermission)
            return true;

        return false;
    }

    @Override
    public boolean checkUserOrGroupPermission(User user, Document doc, PermissionType permission) {
        Optional<DocumentPermission> userPermission = documentPermissionRepository
                .findByDocumentAndUserAndPermissionType(doc, user, permission);

        if (userPermission.isPresent()) {
            return true;
        }

        List<UserGroup> userGroups = this.userGroupService.getGroupsByUser(user);
        if (!userGroups.isEmpty()) {
            Optional<DocumentPermission> groupPermission = documentPermissionRepository
                    .findByDocumentAndGroupInAndPermissionType(doc, userGroups, permission);

            if (groupPermission.isPresent()) {
                return true;
            }
        }

        return false;
    }
}
