package com.vpgh.dms.service;

import com.vpgh.dms.model.entity.Folder;
import com.vpgh.dms.model.entity.User;

public interface FolderPermissionService {

    boolean checkCanEdit(User user, Folder folder);

    boolean checkUserOrGroupPermission(User user, Folder folder, String permission);
}
