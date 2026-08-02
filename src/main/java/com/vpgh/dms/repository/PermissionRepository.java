package com.vpgh.dms.repository;
import java.util.UUID;

import com.vpgh.dms.model.entity.Permission;
import com.vpgh.dms.model.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID>, JpaSpecificationExecutor<Permission> {
    List<Permission> findByIdIn(List<UUID> id);

    Optional<Permission> findById(UUID id);

    boolean existsByApiPathAndMethodAndIdNot(String apiPath, String method, UUID id);

    Permission save(Permission permission);

    Page<Permission> findAll(Specification<Permission> specification, Pageable pageable);

    void deleteById(UUID id);

    List<Permission> findAllByRoles(Role role);
}
