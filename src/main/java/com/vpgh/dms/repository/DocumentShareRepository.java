package com.vpgh.dms.repository;

import com.vpgh.dms.model.constant.ShareType;
import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.DocumentShare;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.model.entity.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentShareRepository extends JpaRepository<DocumentShare, Integer> {
    Optional<DocumentShare> findByDocumentAndUserAndShareType(Document document, User user, ShareType shareType);

    Optional<DocumentShare> findByDocumentAndGroupInAndShareType(Document document, List<UserGroup> groups, ShareType shareType);

    Optional<DocumentShare> findByDocumentAndUser(Document document, User user);

    List<DocumentShare> findByDocument(Document document);

    void deleteByDocumentAndUserIn(Document doc, List<User> users);

}