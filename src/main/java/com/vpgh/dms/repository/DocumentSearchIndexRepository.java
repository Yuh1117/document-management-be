package com.vpgh.dms.repository;

import com.vpgh.dms.model.entity.DocumentSearchIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentSearchIndexRepository extends JpaRepository<DocumentSearchIndex, Integer> {
}
