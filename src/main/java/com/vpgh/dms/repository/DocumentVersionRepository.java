package com.vpgh.dms.repository;

import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.DocumentVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Integer>, JpaSpecificationExecutor<DocumentVersion> {
    Integer countByDocument(Document document);

    Page<DocumentVersion> findAll(Specification<DocumentVersion> specification, Pageable pageable);
}
