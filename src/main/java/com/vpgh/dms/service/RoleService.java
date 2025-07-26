package com.vpgh.dms.service;

import com.vpgh.dms.model.entity.Role;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface RoleService {
    Role getRoleByName(String name);

    boolean existsById(Integer id);

    Role getRoleById(Integer id);

    Page<Role> getAllRoles(Map<String, String> params);

    Role save(Role role);

    boolean existsByNameAndIdNot(String name, Integer id);

    void deleteRoleById(Integer id);
}
