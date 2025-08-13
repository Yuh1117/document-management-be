package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.dto.RoleDTO;
import com.vpgh.dms.model.entity.Role;
import com.vpgh.dms.repository.PermissionRepository;
import com.vpgh.dms.repository.RoleRepository;
import com.vpgh.dms.service.RoleService;
import com.vpgh.dms.service.specification.RoleSpecification;
import com.vpgh.dms.util.PageSize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PermissionRepository permissionRepository;

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

    @Override
    public Page<Role> getAllRoles(Map<String, String> params) {
        Specification<Role> combinedSpec = Specification.allOf();
        Pageable pageable = Pageable.unpaged();
        if (params != null) {
            int page = Integer.parseInt(params.get("page"));
            String kw = params.get("kw");

            pageable = PageRequest.of(page - 1, PageSize.ROLE_PAGE_SIZE.getSize(),
                    Sort.by(Sort.Order.asc("id")));
            if (kw != null && !kw.isEmpty()) {
                Specification<Role> spec = RoleSpecification.filterByKeyword(params.get("kw"));
                combinedSpec = combinedSpec.and(spec);
            }
        }

        return this.roleRepository.findAll(combinedSpec, pageable);
    }

    @Override
    public Role save(Role role) {
        if (role.getPermissions() != null) {
            List<Integer> ids = role.getPermissions().stream().map(p -> p.getId()).collect(Collectors.toList());
            role.setPermissions(new HashSet<>(this.permissionRepository.findByIdIn(ids)));
        }
        return this.roleRepository.save(role);
    }

    @Override
    public Role handleCreateRole(RoleDTO dto) {
        Role role = new Role();
        role.setName(dto.getName());
        role.setDescription(dto.getDescription());
        role.setPermissions(dto.getPermissions() != null ? new HashSet<>(dto.getPermissions()) : null);
        return save(role);
    }

    @Override
    public Role handleUpdateRole(Integer id, RoleDTO dto) {
        Role role = getRoleById(id);
        if (role != null) {
            role.setName(dto.getName());
            role.setDescription(dto.getDescription());
            if (dto.getPermissions() != null) {
                role.setPermissions(new HashSet<>(dto.getPermissions()));
            } else {
                role.setPermissions(null);
            }
            return save(role);
        }
        return null;
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Integer id) {
        return this.roleRepository.existsByNameAndIdNot(name, id);
    }

    @Override
    public void deleteRoleById(Integer id) {
        Role role = getRoleById(id);
        if (!role.getPermissions().isEmpty()) {
            throw new DataIntegrityViolationException("");
        }
        this.roleRepository.deleteById(id);
    }

    @Override
    public long count() {
        return this.roleRepository.count();
    }

    @Override
    public List<Role> saveAll(List<Role> roles) {
        return this.roleRepository.saveAll(roles);
    }

    @Override
    public RoleDTO convertRoleToRoleDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        return dto;
    }
}
