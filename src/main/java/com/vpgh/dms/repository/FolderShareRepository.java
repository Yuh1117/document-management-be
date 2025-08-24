package com.vpgh.dms.repository;

import com.vpgh.dms.model.constant.ShareType;
import com.vpgh.dms.model.entity.Folder;
import com.vpgh.dms.model.entity.FolderShare;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.model.entity.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderShareRepository extends JpaRepository<FolderShare, Integer> {
    Optional<FolderShare> findByFolderAndUserAndShareType(Folder folder, User user, ShareType shareType);

    Optional<FolderShare> findByFolderAndGroupInAndShareType(Folder folder, List<UserGroup> groups, ShareType shareType);

    Optional<FolderShare> findByFolderAndUser(Folder folder, User user);

    List<FolderShare> findByFolder(Folder folder);

    void deleteByFolderInAndUserIn(List<Folder> folders, List<User> users);
}