package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.entity.Permission;
import com.vpgh.dms.repository.PermissionRepository;
import com.vpgh.dms.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

    @Override
    public List<Permission> findPermissionsByIdIn(List<Integer> ids) {
        return this.permissionRepository.findByIdIn(ids);
    }

    @Override
    public Permission getPermissionById(Integer id) {
        return this.permissionRepository.findById(id);
    }
}
