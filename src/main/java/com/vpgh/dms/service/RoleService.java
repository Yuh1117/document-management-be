package com.vpgh.dms.service;
import java.util.UUID;

import com.vpgh.dms.model.dto.RoleDTO;
import com.vpgh.dms.model.entity.Role;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface RoleService {
    Role getRoleByName(String name);

    boolean existsById(UUID id);

    Role getRoleById(UUID id);

    Page<Role> getAllRoles(Map<String, String> params);

    Role save(Role role);

    boolean existsByNameAndIdNot(String name, UUID id);

    void deleteRoleById(UUID id);

    Role handleCreateRole(RoleDTO dto);

    Role handleUpdateRole(Role role, RoleDTO dto);

    long count();

    List<Role> saveAll(List<Role> roles);

    RoleDTO convertRoleToRoleDTO(Role role);
}
