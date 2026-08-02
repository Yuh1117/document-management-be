package com.vpgh.dms.repository;

import com.vpgh.dms.model.constant.ShareType;
import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.DocumentShare;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.model.entity.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface DocumentShareRepository extends JpaRepository<DocumentShare, UUID> {
    Optional<DocumentShare> findByDocumentAndUserAndShareType(Document document, User user, ShareType shareType);

    Optional<DocumentShare> findByDocumentAndGroupInAndShareType(Document document, List<UserGroup> groups, ShareType shareType);

    Optional<DocumentShare> findByDocumentAndUser(Document document, User user);

    List<DocumentShare> findByDocument(Document document);

    void deleteByDocumentAndUserIn(Document doc, List<User> users);

    void deleteByDocumentInAndUserIn(List<Document> docs, List<User> users);

    @Query("SELECT ds.document.id FROM DocumentShare ds " +
           "WHERE ds.document IN :docs " +
           "AND (ds.user = :user OR ds.group IN :groups)")
    Set<UUID> findViewableDocumentIds(
            @Param("docs") List<Document> docs,
            @Param("user") User user,
            @Param("groups") List<UserGroup> groups);
}
