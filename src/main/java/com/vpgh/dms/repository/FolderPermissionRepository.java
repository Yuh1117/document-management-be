package com.vpgh.dms.repository;

import com.vpgh.dms.model.constant.PermissionType;
import com.vpgh.dms.model.entity.Folder;
import com.vpgh.dms.model.entity.FolderPermission;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.model.entity.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderPermissionRepository extends JpaRepository<FolderPermission, Integer> {
    Optional<FolderPermission> findByFolderAndUserAndPermissionType(Folder folder, User user, PermissionType permissionType);

    Optional<FolderPermission> findByFolderAndGroupInAndPermissionType(Folder folder, List<UserGroup> groups, PermissionType permissionType);
}