package com.vpgh.dms.repository;

import com.vpgh.dms.model.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, InternalError> {
    List<Permission> findByIdIn(List<Integer> id);

    Permission findById(Integer id);
}
