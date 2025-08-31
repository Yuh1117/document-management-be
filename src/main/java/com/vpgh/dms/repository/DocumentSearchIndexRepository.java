package com.vpgh.dms.repository;

import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.DocumentSearchIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentSearchIndexRepository extends JpaRepository<DocumentSearchIndex, Integer> {
    Optional<DocumentSearchIndex> findById(Integer id);

    DocumentSearchIndex findByDocument(Document document);
}
