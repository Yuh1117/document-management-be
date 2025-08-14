package com.vpgh.dms.repository;

import com.vpgh.dms.model.constant.PermissionType;
import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.DocumentPermission;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.model.entity.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentPermissionRepository extends JpaRepository<DocumentPermission, Integer> {
    Optional<DocumentPermission> findByDocumentAndUserAndPermissionType(Document document, User user, PermissionType permissionType);

    Optional<DocumentPermission> findByDocumentAndGroupInAndPermissionType(Document document, List<UserGroup> groups, PermissionType permissionType);
}