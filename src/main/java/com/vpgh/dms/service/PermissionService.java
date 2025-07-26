package com.vpgh.dms.service;

import com.vpgh.dms.model.entity.Permission;

import java.util.List;

public interface PermissionService {
    List<Permission> findPermissionsByIdIn(List<Integer> ids);

    Permission getPermissionById(Integer id);
}
