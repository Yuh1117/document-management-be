package com.vpgh.dms.service;

import com.vpgh.dms.model.constant.PermissionType;
import com.vpgh.dms.model.entity.Folder;
import com.vpgh.dms.model.entity.User;

public interface FolderPermissionService {
    boolean checkCanView(User user, Folder folder);

    boolean checkCanEdit(User user, Folder folder);

    boolean checkUserOrGroupPermission(User user, Folder folder, PermissionType permission);
}
