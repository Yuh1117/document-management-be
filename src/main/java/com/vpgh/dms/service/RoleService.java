package com.vpgh.dms.service;

import com.vpgh.dms.model.entity.Role;

public interface RoleService {
    Role getRoleByName(String name);

    boolean existsById(Integer id);

    Role getRoleById(Integer id);
}
