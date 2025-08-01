package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.service.DocumentPermissionService;
import com.vpgh.dms.util.exception.ForbiddenException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class DocumentPermissionServiceImpl implements DocumentPermissionService {

    public void checkCanView(User user, Document doc) {
        if (Objects.equals(doc.getCreatedBy().getId(), user.getId())) return;

        boolean hasPermission = checkUserOrGroupPermission(user, doc, "VIEW");
        if (!hasPermission) {
            throw new ForbiddenException("Bạn không có quyền xem tài liệu này");
        }
    }

    public void checkCanEdit(User user, Document doc) {
        if (Objects.equals(doc.getCreatedBy().getId(), user.getId())) return;

        boolean hasPermission = checkUserOrGroupPermission(user, doc, "EDIT");
        if (!hasPermission) {
            throw new ForbiddenException("Bạn không có quyền chỉnh sửa/xoá tài liệu này");
        }
    }

    public boolean checkUserOrGroupPermission(User user, Document doc, String permission) {
        return true;
    }
}
