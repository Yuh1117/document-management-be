package com.vpgh.dms.repository;

import com.vpgh.dms.model.entity.DocumentSummary;
import com.vpgh.dms.model.entity.SummaryFeedback;
import com.vpgh.dms.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SummaryFeedbackRepository extends JpaRepository<SummaryFeedback, Integer> {

    Optional<SummaryFeedback> findBySummaryAndUser(DocumentSummary summary, User user);

    List<SummaryFeedback> findBySummary(DocumentSummary summary);

    @Query("SELECT COUNT(f) FROM SummaryFeedback f WHERE f.summary.document.id = :docId AND f.isHelpful = true")
    long countHelpfulByDocumentId(@Param("docId") Integer docId);

    @Query("SELECT COUNT(f) FROM SummaryFeedback f WHERE f.summary.document.id = :docId AND f.isHelpful = false")
    long countNotHelpfulByDocumentId(@Param("docId") Integer docId);

    @Query("SELECT COUNT(f) FROM SummaryFeedback f WHERE f.summary.modelName = :modelName AND f.isHelpful = true")
    long countHelpfulByModelName(@Param("modelName") String modelName);

    @Query("SELECT COUNT(f) FROM SummaryFeedback f WHERE f.summary.modelName = :modelName AND f.isHelpful = false")
    long countNotHelpfulByModelName(@Param("modelName") String modelName);

    @Query("SELECT DISTINCT f.summary.modelName FROM SummaryFeedback f WHERE f.summary.modelName IS NOT NULL")
    List<String> findDistinctModelNames();
}
