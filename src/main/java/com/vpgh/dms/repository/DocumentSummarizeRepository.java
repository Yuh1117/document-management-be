package com.vpgh.dms.repository;
import java.util.UUID;

import com.vpgh.dms.model.entity.DocumentSummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentSummarizeRepository extends JpaRepository<DocumentSummary, UUID> {

}