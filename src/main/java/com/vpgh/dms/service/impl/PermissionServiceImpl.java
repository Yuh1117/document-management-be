package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.dto.PermissionDTO;
import com.vpgh.dms.model.entity.Permission;
import com.vpgh.dms.repository.PermissionRepository;
import com.vpgh.dms.service.PermissionService;
import com.vpgh.dms.service.specification.PermissionSpecification;
import com.vpgh.dms.util.PageSize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        Optional<Permission> permission = this.permissionRepository.findById(id);
        return permission.isPresent() ? permission.get() : null;
    }

    @Override
    public boolean existsByApiPathAndMethodAndIdNot(String apiPath, String method, Integer id) {
        return this.permissionRepository.existsByApiPathAndMethodAndIdNot(apiPath, method, id);
    }

    @Override
    public Permission save(Permission permission) {
        return this.permissionRepository.save(permission);
    }

    @Override
    public Permission handleCreatePermission(PermissionDTO dto) {
        Permission permission = new Permission();
        permission.setName(dto.getName());
        permission.setApiPath(dto.getApiPath());
        permission.setMethod(dto.getMethod());
        permission.setModule(dto.getModule());
        return save(permission);
    }

    @Override
    public Page<Permission> getAllPermission(Map<String, String> params) {
        int page = Integer.parseInt(params.get("page"));
        String kw = params.get("kw");

        Pageable pageable = PageRequest.of(page - 1, PageSize.ROLE_PAGE_SIZE.getSize());
        Specification<Permission> combinedSpec = Specification.allOf();
        if (kw != null && !kw.isEmpty()) {
            Specification<Permission> spec = PermissionSpecification.filterByKeyword(params.get("kw"));
            combinedSpec = combinedSpec.and(spec);
        }

        return this.permissionRepository.findAll(combinedSpec, pageable);
    }

    @Override
    public Permission handleUpdatePermission(Integer id, PermissionDTO dto) {
        Permission permission = getPermissionById(id);
        if (permission != null) {
            permission.setName(dto.getName());
            permission.setApiPath(dto.getApiPath());
            permission.setMethod(dto.getMethod());
            permission.setModule(dto.getModule());
            return save(permission);
        }
        return null;
    }

    @Override
    public void deletePermissionById(Integer id) {
        Permission permission = getPermissionById(id);
        if (!permission.getRoles().isEmpty()) {
            throw new DataIntegrityViolationException("");
        }
        this.permissionRepository.deleteById(id);
    }
}
