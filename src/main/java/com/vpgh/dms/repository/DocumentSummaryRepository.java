package com.vpgh.dms.repository;

import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.DocumentSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentSummaryRepository extends JpaRepository<DocumentSummary, Integer> {

    Optional<DocumentSummary> findTopByDocumentOrderByCreatedAtDesc(Document document);
}
