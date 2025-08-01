package com.vpgh.dms.service;

import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.User;

public interface DocumentPermissionService {
    void checkCanView(User user, Document doc);

    void checkCanEdit(User user, Document doc);

    boolean checkUserOrGroupPermission(User user, Document doc, String permission);
}
