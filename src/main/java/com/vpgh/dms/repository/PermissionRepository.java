package com.vpgh.dms.repository;

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
public interface PermissionRepository extends JpaRepository<Permission, Integer>, JpaSpecificationExecutor<Permission> {
    List<Permission> findByIdIn(List<Integer> id);

    Optional<Permission> findById(Integer id);

    boolean existsByApiPathAndMethodAndIdNot(String apiPath, String method, Integer id);

    Permission save(Permission permission);

    Page<Permission> findAll(Specification<Permission> specification, Pageable pageable);

    void deleteById(Integer id);

    List<Permission> findAllByRoles(Role role);
}
