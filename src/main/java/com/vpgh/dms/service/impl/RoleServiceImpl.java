package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.entity.Role;
import com.vpgh.dms.repository.RoleRepository;
import com.vpgh.dms.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public Role getRoleByName(String name) {
        return this.roleRepository.findByName(name);
    }

    @Override
    public boolean existsById(Integer id) {
        return this.roleRepository.existsById(id);
    }

    @Override
    public Role getRoleById(Integer id) {
        Optional<Role> role = this.roleRepository.findById(id);
        return role.isPresent() ? role.get() : null;
    }


}
