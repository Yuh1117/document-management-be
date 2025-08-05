package com.vpgh.dms.repository;

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
public interface DocumentRepository extends JpaRepository<Document, Integer> {
    Document save(Document document);

    Document findByStoredFilename(String storedFileName);

    Optional<Document> findById(Integer integer);

    List<Document> findAllById(Iterable<Integer> ids);

    boolean existsByNameAndFolderAndIdNot(String name, Folder folder, Integer excludeId);

    List<Document> findByFolderId(Integer id);

    Page<Document> findByFolderAndCreatedByAndIsDeletedFalse(Folder folder, User createdBy, Pageable pageable);

    Page<Document> findByFolderAndCreatedByAndIsDeletedTrue(Folder folder, User createdBy, Pageable pageable);

    Page<Document> findAll(Specification<Document> specification, Pageable pageable);
}
