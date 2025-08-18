package com.vpgh.dms.repository;

import com.vpgh.dms.model.dto.response.FileItemProjection;
import com.vpgh.dms.model.entity.Folder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FileItemRepository extends JpaRepository<Folder, Integer> {

    @Query(value = """
            SELECT f.id AS id, f.name AS name, 'folder' AS type,
                   f.created_at AS createdAt, f.updated_at AS updatedAt, f.is_deleted AS isDeleted,
                   u.id AS createdById, u.email AS createdByEmail
            FROM folders f
            JOIN users u ON f.created_by = u.id
            WHERE f.created_by = :userId AND f.is_deleted = :deleted
              AND ( (:parentId = -1 AND f.parent_id IS NULL) OR f.parent_id = :parentId )
            
            UNION ALL
            
            SELECT d.id AS id, d.name AS name, 'document' AS type,
                   d.created_at AS createdAt, d.updated_at AS updatedAt, d.is_deleted AS isDeleted,
                   u.id AS createdById, u.email AS createdByEmail
            FROM documents d
            JOIN users u ON d.created_by = u.id
            WHERE d.created_by = :userId AND d.is_deleted = :deleted
              AND ( (:parentId = -1 AND d.folder_id IS NULL) OR d.folder_id = :parentId )
            
            ORDER BY name ASC
            """,
            countQuery = """
                    SELECT COUNT(*) FROM (
                        SELECT f.id
                        FROM folders f
                        WHERE f.created_by = :userId AND f.is_deleted = :deleted
                          AND ( (:parentId = -1 AND f.parent_id IS NULL) OR f.parent_id = :parentId )
                    
                        UNION ALL
                    
                        SELECT d.id
                        FROM documents d
                        WHERE d.created_by = :userId AND d.is_deleted = :deleted
                          AND ( (:parentId = -1 AND d.folder_id IS NULL) OR d.folder_id = :parentId )
                    ) AS total
                    """,
            nativeQuery = true
    )
    Page<FileItemProjection> findAllByUserAndParent(@Param("userId") Integer userId,
                                                    @Param("parentId") Integer parentId,
                                                    @Param("deleted") Boolean deleted,
                                                    Pageable pageable);

    @Query(value = """
            SELECT f.id AS id, f.name AS name, 'folder' AS type,
                   f.created_at AS createdAt, f.updated_at AS updatedAt, f.is_deleted AS isDeleted,
                   u.id AS createdById, u.email AS createdByEmail
            FROM folders f
            JOIN users u ON f.created_by = u.id
            WHERE  f.is_deleted = :deleted AND f.parent_id = :folderId
            
            UNION ALL
            
            SELECT d.id AS id, d.name AS name, 'document' AS type,
                   d.created_at AS createdAt, d.updated_at AS updatedAt, d.is_deleted AS isDeleted,
                   u.id AS createdById, u.email AS createdByEmail
            FROM documents d
            JOIN users u ON d.created_by = u.id
            WHERE d.is_deleted = :deleted AND d.folder_id = :folderId
            
            ORDER BY name ASC
            """,
            countQuery = """
                    SELECT COUNT(*) FROM (
                        SELECT f.id
                        FROM folders f
                        WHERE f.is_deleted = :deleted AND f.parent_id = :folderId
                    
                        UNION ALL
                    
                        SELECT d.id
                        FROM documents d
                        WHERE d.is_deleted = :deleted AND d.folder_id = :folderId
                    ) AS total
                    """,
            nativeQuery = true
    )
    Page<FileItemProjection> findFolderFiles(@Param("folderId") Integer folderId,
                                             @Param("deleted") Boolean deleted,
                                             Pageable pageable);
}