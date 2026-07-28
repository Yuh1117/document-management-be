package com.vpgh.dms.repository;
import java.util.UUID;

import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.DocumentVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, UUID>, JpaSpecificationExecutor<DocumentVersion> {
    Integer countByDocument(Document document);

    Page<DocumentVersion> findAll(Specification<DocumentVersion> specification, Pageable pageable);

    List<DocumentVersion> findByDocumentOrderByVersionNumberAsc(Document document);
}