package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.service.DocumentPermissionService;
import com.vpgh.dms.util.exception.ForbiddenException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class DocumentPermissionServiceImpl implements DocumentPermissionService {

    @Override
    public boolean checkCanView(User user, Document doc) {
        if (Objects.equals(doc.getCreatedBy().getId(), user.getId()))
            return true;

        boolean hasPermission = checkUserOrGroupPermission(user, doc, "VIEW");
        if (hasPermission)
            return true;

        return false;
    }

    @Override
    public boolean checkCanEdit(User user, Document doc) {
        if (Objects.equals(doc.getCreatedBy().getId(), user.getId()))
            return true;

        boolean hasPermission = checkUserOrGroupPermission(user, doc, "EDIT");
        if (hasPermission)
            return true;

        return false;
    }

    @Override
    public boolean checkUserOrGroupPermission(User user, Document doc, String permission) {
        return false;
    }
}
