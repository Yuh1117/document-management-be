package com.vpgh.dms.service;

import com.vpgh.dms.model.dto.PermissionDTO;
import com.vpgh.dms.model.entity.Permission;
import com.vpgh.dms.model.entity.Role;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PermissionService {
    List<Permission> findPermissionsByIdIn(List<UUID> ids);

    Permission getPermissionById(UUID id);

    boolean existsByApiPathAndMethodAndIdNot(String apiPath, String method, UUID id);

    Permission save(Permission permission);

    Permission handleCreatePermission(PermissionDTO dto);

    Page<Permission> getAllPermission(Map<String, String> params);

    Permission handleUpdatePermission(Permission permission, PermissionDTO dto);

    void deletePermissionById(UUID id);

    long count();

    List<Permission> saveAll(List<Permission> permissions);

    List<Permission> getPermissionsByRole(Role role);

    void evictPermissionsCache();
}
