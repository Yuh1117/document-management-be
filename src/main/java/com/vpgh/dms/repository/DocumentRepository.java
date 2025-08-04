package com.vpgh.dms.repository;

import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.Folder;
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

    List<Document> findByFolderIdAndIsDeletedFalse(Integer folderId);

    List<Document> findByFolderIdAndIsDeletedTrue(Integer folderId);
}
