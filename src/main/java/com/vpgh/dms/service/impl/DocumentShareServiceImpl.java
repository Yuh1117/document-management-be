package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.constant.ShareType;
import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.DocumentShare;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.model.entity.UserGroup;
import com.vpgh.dms.repository.DocumentShareRepository;
import com.vpgh.dms.service.DocumentShareService;
import com.vpgh.dms.service.UserGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class DocumentShareServiceImpl implements DocumentShareService {
    @Autowired
    private DocumentShareRepository documentShareRepository;
    @Autowired
    private UserGroupService userGroupService;

    @Override
    public boolean checkCanView(User user, Document doc) {
        if (Objects.equals(doc.getCreatedBy().getId(), user.getId()))
            return true;

        boolean hasPermission = checkUserOrGroupPermission(user, doc, ShareType.VIEW);
        if (hasPermission)
            return true;

        return false;
    }

    @Override
    public boolean checkCanEdit(User user, Document doc) {
        if (Objects.equals(doc.getCreatedBy().getId(), user.getId()))
            return true;

        boolean hasPermission = checkUserOrGroupPermission(user, doc, ShareType.EDIT);
        if (hasPermission)
            return true;

        return false;
    }

    @Override
    public boolean checkUserOrGroupPermission(User user, Document doc, ShareType permission) {
        Optional<DocumentShare> userPermission = documentShareRepository
                .findByDocumentAndUserAndShareType(doc, user, permission);

        if (userPermission.isPresent()) {
            return true;
        }

        List<UserGroup> userGroups = this.userGroupService.getGroupsByUser(user);
        if (!userGroups.isEmpty()) {
            Optional<DocumentShare> groupPermission = documentShareRepository
                    .findByDocumentAndGroupInAndShareType(doc, userGroups, permission);

            if (groupPermission.isPresent()) {
                return true;
            }
        }

        return false;
    }
}
