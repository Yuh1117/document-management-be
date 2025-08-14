package com.vpgh.dms.repository;

import com.vpgh.dms.model.entity.FolderPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FolderPermissionRepository extends JpaRepository<FolderPermission, Integer> {

}