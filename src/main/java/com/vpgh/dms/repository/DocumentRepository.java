package com.vpgh.dms.repository;
import java.util.UUID;

import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.Folder;
import com.vpgh.dms.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
    Document save(Document document);

    Document findByStoredFilename(String storedFileName);

    Optional<Document> findById(UUID id);

    List<Document> findByIdIn(List<UUID> ids);

    List<Document> findByFolderIn(List<Folder> folders);

    boolean existsByNameAndFolderAndIsDeletedFalseAndIdNot(String name, Folder folder, UUID excludeId);

    boolean existsByNameAndCreatedByAndFolderIsNullAndIsDeletedFalseAndIdNot(String name, User createdBy, UUID id);

    List<Document> findByFolderId(UUID id);

    Page<Document> findAll(Specification<Document> specification, Pageable pageable);

    Optional<Document> findFirstByNameAndFolderAndIsDeletedFalse(String name, Folder folder);

    Optional<Document> findFirstByNameAndCreatedByAndFolderIsNullAndIsDeletedFalse(String name, User createdBy);

    List<Document> findByFolderAndIsDeletedFalse(Folder folder);

    List<Document> findByFolderAndIsDeletedTrue(Folder folder);
}