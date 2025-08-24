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
public interface FileRepository extends JpaRepository<Folder, Integer> {

    @Query(value = """
            SELECT f.id AS id, f.name AS name, 'folder' AS type,
                   f.created_at AS createdAt, f.updated_at AS updatedAt, f.is_deleted AS isDeleted,
                   u.id AS createdById, u.email AS createdByEmail,
                   NULL as description, 'OWNER' as permission,
                   0 as sortType
            FROM folders f
            JOIN users u ON f.created_by = u.id
            WHERE f.created_by = :userId AND f.is_deleted = :deleted
               AND (
                      :parentId = -1 OR
                      (:parentId IS NULL AND f.parent_id IS NULL) OR
                      f.parent_id = :parentId
                  )
               AND (:keyword IS NULL OR LOWER(f.name) LIKE LOWER(CONCAT(:keyword, '%')))
            
            UNION ALL
            
            SELECT d.id AS id, d.name AS name, 'document' AS type,
                   d.created_at AS createdAt, d.updated_at AS updatedAt, d.is_deleted AS isDeleted,
                   u.id AS createdById, u.email AS createdByEmail,
                   d.description as description, 'OWNER' as permission,
                   1 as sortType
            FROM documents d
            JOIN users u ON d.created_by = u.id
            WHERE d.created_by = :userId AND d.is_deleted = :deleted
              AND (
                      :parentId = -1 OR
                      (:parentId IS NULL AND d.folder_id IS NULL) OR
                      d.folder_id = :parentId
                  )
              AND (:keyword IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT(:keyword, '%')))
            
            ORDER BY sortType ASC, name ASC
            """,
            countQuery = """    
                    SELECT COUNT(*) FROM (
                        SELECT f.id
                        FROM folders f
                        WHERE f.created_by = :userId AND f.is_deleted = :deleted
                          AND (
                              :parentId = -1 OR
                              (:parentId IS NULL AND f.parent_id IS NULL) OR
                              f.parent_id = :parentId
                          )
                          AND (:keyword IS NULL OR LOWER(f.name) LIKE LOWER(CONCAT(:keyword, '%')))
                    
                        UNION ALL
                    
                        SELECT d.id
                        FROM documents d
                        WHERE d.created_by = :userId AND d.is_deleted = :deleted
                          AND (
                              :parentId = -1 OR
                              (:parentId IS NULL AND d.folder_id IS NULL) OR
                              d.folder_id = :parentId
                          )
                          AND (:keyword IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT(:keyword, '%')))
                    ) AS total
                    """,
            nativeQuery = true
    )
    Page<FileItemProjection> findAllByUserAndParent(@Param("userId") Integer userId,
                                                    @Param("parentId") Integer parentId,
                                                    @Param("deleted") Boolean deleted,
                                                    @Param("keyword") String keyword,
                                                    Pageable pageable);

    @Query(value = """
            SELECT f.id AS id, f.name AS name, 'folder' AS type,
                   f.created_at AS createdAt, f.updated_at AS updatedAt, f.is_deleted AS isDeleted,
                   u.id AS createdById, u.email AS createdByEmail,
                   NULL as description, 'OWNER' as permission,
                   0 as sortType
            FROM folders f
            JOIN users u ON f.created_by = u.id
            WHERE f.created_by = :userId AND f.is_deleted = true
                AND (f.parent_id IS NULL OR EXISTS (
                    SELECT 1 FROM folders p WHERE p.id = f.parent_id AND p.is_deleted = false
                  ))
            
            UNION ALL
            
            SELECT d.id AS id, d.name AS name, 'document' AS type,
                   d.created_at AS createdAt, d.updated_at AS updatedAt, d.is_deleted AS isDeleted,
                   u.id AS createdById, u.email AS createdByEmail,
                   d.description as description, 'OWNER' as permission,
                   1 as sortType
            FROM documents d
            JOIN users u ON d.created_by = u.id
            WHERE d.created_by = :userId AND d.is_deleted = true
              AND (d.folder_id IS NULL OR EXISTS (
                    SELECT 1 FROM folders p WHERE p.id = d.folder_id AND p.is_deleted = false
                  ))
            
            ORDER BY sortType ASC, name ASC
            """,
            countQuery = """    
                    SELECT COUNT(*) FROM (
                        SELECT f.id
                        FROM folders f
                        WHERE f.created_by = :userId AND f.is_deleted = :deleted
                          AND (f.parent_id IS NULL OR EXISTS (
                            SELECT 1 FROM folders p WHERE p.id = f.parent_id AND p.is_deleted = false
                          ))
                    
                        UNION ALL
                    
                        SELECT d.id
                        FROM documents d
                        WHERE d.created_by = :userId AND d.is_deleted = :deleted
                          AND (d.folder_id IS NULL OR EXISTS (
                            SELECT 1 FROM folders p WHERE p.id = d.folder_id AND p.is_deleted = false
                          ))
                    ) AS total
                    """,
            nativeQuery = true
    )
    Page<FileItemProjection> findTrashFiles(@Param("userId") Integer userId, Pageable pageable);


    @Query(value = """
            SELECT f.id AS id, f.name AS name, 'folder' AS type,
                   f.created_at AS createdAt, f.updated_at AS updatedAt, f.is_deleted AS isDeleted,
                   u.id AS createdById, u.email AS createdByEmail,
                   NULL as description,
                   CASE 
                       WHEN f.created_by = :userId THEN 'OWNER'
                       ELSE fs.share_type
                   END as permission,
                   0 as sortType
            FROM folders f
            JOIN users u ON f.created_by = u.id
            LEFT JOIN folder_shares fs ON f.id = fs.folder_id AND fs.user_id = :userId
            WHERE f.is_deleted = :deleted AND f.parent_id = :folderId
            
            UNION ALL
            
            SELECT d.id AS id, d.name AS name, 'document' AS type,
                   d.created_at AS createdAt, d.updated_at AS updatedAt, d.is_deleted AS isDeleted,
                   u.id AS createdById, u.email AS createdByEmail,
                   d.description as description,
                   CASE 
                       WHEN d.created_by = :userId THEN 'OWNER'
                       ELSE ds.share_type
                   END as permission,
                   1 as sortType
            FROM documents d
            JOIN users u ON d.created_by = u.id
            LEFT JOIN document_shares ds ON d.id = ds.document_id AND ds.user_id = :userId
            WHERE d.is_deleted = :deleted AND d.folder_id = :folderId
            
            ORDER BY sortType ASC, name ASC
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
    Page<FileItemProjection> findFolderFiles(@Param("userId") Integer userId,
                                             @Param("folderId") Integer folderId,
                                             @Param("deleted") Boolean deleted,
                                             Pageable pageable);


    @Query(value = """
            SELECT f.id AS id, f.name AS name, 'folder' AS type,
                   f.created_at AS createdAt, f.updated_at AS updatedAt, f.is_deleted AS isDeleted,
                   u.id AS createdById, u.email AS createdByEmail,
                   NULL as description, fs.share_type as permission,
                   0 as sortType
            FROM folders f
            JOIN users u ON f.created_by = u.id
            JOIN folder_shares fs ON f.id = fs.folder_id
            WHERE fs.user_id = :userId AND f.is_deleted = false
                AND  (f.parent_id IS NULL OR NOT EXISTS (
                        SELECT 1 
                        FROM folder_shares fs2 
                        JOIN folders f2 ON fs2.folder_id = f2.id
                        WHERE fs2.user_id = :userId AND f2.id = f.parent_id AND f2.is_deleted = false
                    )
                )
            
            UNION ALL
            
            SELECT d.id AS id, d.name AS name, 'document' AS type,
                   d.created_at AS createdAt, d.updated_at AS updatedAt, d.is_deleted AS isDeleted,
                   u.id AS createdById, u.email AS createdByEmail,
                   d.description as description, ds.share_type as permission,
                   1 as sortType
            FROM documents d
            JOIN users u ON d.created_by = u.id
            JOIN document_shares ds ON d.id = ds.document_id
            WHERE ds.user_id = :userId AND d.is_deleted = false
                AND  (d.folder_id IS NULL OR NOT EXISTS (
                        SELECT 1 
                        FROM folder_shares fs3
                        JOIN folders f3 ON fs3.folder_id = f3.id
                        WHERE fs3.user_id = :userId AND f3.id = d.folder_id AND f3.is_deleted = false
                    )
                )
                        
            ORDER BY sortType ASC, name ASC
            
            """,
            countQuery = """
                        SELECT COUNT(*) FROM (
                            SELECT f.id
                            FROM folders f
                            JOIN folder_shares fs ON f.id = fs.folder_id
                            WHERE fs.user_id = :userId AND f.is_deleted = false
                                AND  (f.parent_id IS NULL OR NOT EXISTS (
                                    SELECT 1 
                                    FROM folder_shares fs2 
                                    JOIN folders f2 ON fs2.folder_id = f2.id
                                    WHERE fs2.user_id = :userId AND f2.id = f.parent_id AND f2.is_deleted = false
                                   )
                              )
                                                
                            UNION ALL
                    
                            SELECT d.id
                            FROM documents d
                            JOIN document_shares ds ON d.id = ds.document_id
                            WHERE ds.user_id = :userId AND d.is_deleted = false
                                AND  (d.folder_id IS NULL OR NOT EXISTS (
                                        SELECT 1 
                                        FROM folder_shares fs3
                                        JOIN folders f3 ON fs3.folder_id = f3.id
                                        WHERE fs3.user_id = :userId AND f3.id = d.folder_id AND f3.is_deleted = false
                                    )
                                )
                        ) AS total
                    """,
            nativeQuery = true
    )
    Page<FileItemProjection> findSharedFiles(@Param("userId") Integer userId, Pageable pageable);
}