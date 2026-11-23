package com.vpgh.dms.repository;

import com.vpgh.dms.model.dto.response.FileItemProjection;
import com.vpgh.dms.model.entity.Folder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<Folder, UUID> {

    String BROWSE_FOLDER_FILTER = """
             AND :includeFolders = true
             AND COALESCE(f.updated_at, f.created_at, :updatedFrom) >= :updatedFrom
            """;

    String BROWSE_DOCUMENT_FILTER = """
             AND :includeDocuments = true
             AND (CAST(:mimeType AS text) IS NULL OR d.mime_type LIKE CAST(:mimeType AS text))
             AND COALESCE(d.updated_at, d.created_at, :updatedFrom) >= :updatedFrom
            """;

    String BROWSE_ORDER = """
            ORDER BY t.sortType ASC,
                CASE WHEN :sortBy = 'name' AND :sortDir = 'asc' THEN LOWER(t.name) END ASC,
                CASE WHEN :sortBy = 'name' AND :sortDir = 'desc' THEN LOWER(t.name) END DESC,
                CASE WHEN :sortBy = 'updatedAt' AND :sortDir = 'asc' THEN COALESCE(t.updatedAt, t.createdAt) END ASC,
                CASE WHEN :sortBy = 'updatedAt' AND :sortDir = 'desc' THEN COALESCE(t.updatedAt, t.createdAt) END DESC,
                CASE WHEN :sortBy = 'size' AND :sortDir = 'asc' THEN t.fileSize END ASC,
                CASE WHEN :sortBy = 'size' AND :sortDir = 'desc' THEN t.fileSize END DESC,
                LOWER(t.name) ASC, t.id ASC
            """;

    @Query(value = "SELECT * FROM (" + """
            SELECT f.id AS id, f.name AS name, 'folder' AS type,
                   f.created_at AS createdAt, f.updated_at AS updatedAt, f.is_deleted AS isDeleted,
                   u.id AS createdById, u.email AS createdByEmail, u.first_name AS createdByFirstName, u.last_name AS createdByLastName,
                   NULL as description, 'OWNER' as permission, NULL as mime_type, NULL as processingStatus, NULL as fileSize,
                   0 as sortType
            FROM folders f
            JOIN users u ON f.created_by = u.id
            WHERE f.created_by = :userId AND f.is_deleted = :deleted
               AND (
                      :matchAll = true OR
                      (:parentId IS NULL AND f.parent_id IS NULL) OR
                      f.parent_id = :parentId
                  )
               AND (:keyword IS NULL OR LOWER(f.name) LIKE LOWER(CONCAT(:keyword, '%')) ESCAPE '\\')
            """ + BROWSE_FOLDER_FILTER + """


            UNION ALL

            SELECT d.id AS id, d.name AS name, 'document' AS type,
                   d.created_at AS createdAt, d.updated_at AS updatedAt, d.is_deleted AS isDeleted,
                   u.id AS createdById, u.email AS createdByEmail, u.first_name AS createdByFirstName, u.last_name AS createdByLastName,
                   d.description as description, 'OWNER' as permission,  d.mime_type as mime_type, d.processing_status as processingStatus, d.file_size as fileSize,
                   1 as sortType
            FROM documents d
            JOIN users u ON d.created_by = u.id
            WHERE d.created_by = :userId AND d.is_deleted = :deleted
              AND (
                      :matchAll = true OR
                      (:parentId IS NULL AND d.folder_id IS NULL) OR
                      d.folder_id = :parentId
                  )
              AND (:keyword IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT(:keyword, '%')) ESCAPE '\\')
            """ + BROWSE_DOCUMENT_FILTER + ") t " + BROWSE_ORDER, countQuery = """
            SELECT COUNT(*) FROM (
                SELECT f.id
                FROM folders f
                WHERE f.created_by = :userId AND f.is_deleted = :deleted
                  AND (
                      :matchAll = true OR
                      (:parentId IS NULL AND f.parent_id IS NULL) OR
                      f.parent_id = :parentId
                  )
                  AND (:keyword IS NULL OR LOWER(f.name) LIKE LOWER(CONCAT(:keyword, '%')) ESCAPE '\\')
                """ + BROWSE_FOLDER_FILTER + """


                UNION ALL

                SELECT d.id
                FROM documents d
                WHERE d.created_by = :userId AND d.is_deleted = :deleted
                  AND (
                      :matchAll = true OR
                      (:parentId IS NULL AND d.folder_id IS NULL) OR
                      d.folder_id = :parentId
                  )
                  AND (:keyword IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT(:keyword, '%')) ESCAPE '\\')
                """ + BROWSE_DOCUMENT_FILTER + """
            ) AS total
            """, nativeQuery = true)
    Page<FileItemProjection> findAllByUserAndParent(@Param("includeFolders") Boolean includeFolders,
            @Param("includeDocuments") Boolean includeDocuments,
            @Param("mimeType") String mimeType,
            @Param("updatedFrom") Instant updatedFrom,
            @Param("sortBy") String sortBy,
            @Param("sortDir") String sortDir,
            @Param("userId") UUID userId,
            @Param("parentId") UUID parentId,
            @Param("matchAll") Boolean matchAll,
            @Param("deleted") Boolean deleted,
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query(value = """
            SELECT f.id AS id, f.name AS name, 'folder' AS type,
                   f.created_at AS createdAt, f.updated_at AS updatedAt, f.is_deleted AS isDeleted,
                   u.id AS createdById, u.email AS createdByEmail, u.first_name AS createdByFirstName, u.last_name AS createdByLastName,
                   NULL as description, 'OWNER' as permission, NULL as mime_type, NULL as processingStatus, NULL as fileSize,
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
                   u.id AS createdById, u.email AS createdByEmail, u.first_name AS createdByFirstName, u.last_name AS createdByLastName,
                   d.description as description, 'OWNER' as permission, d.mime_type as mime_type, d.processing_status as processingStatus, d.file_size as fileSize,
                   1 as sortType
            FROM documents d
            JOIN users u ON d.created_by = u.id
            WHERE d.created_by = :userId AND d.is_deleted = true
              AND (d.folder_id IS NULL OR EXISTS (
                    SELECT 1 FROM folders p WHERE p.id = d.folder_id AND p.is_deleted = false
                  ))

            ORDER BY sortType ASC, name ASC
            """, countQuery = """
            SELECT COUNT(*) FROM (
                SELECT f.id
                FROM folders f
                WHERE f.created_by = :userId AND f.is_deleted = true
                  AND (f.parent_id IS NULL OR EXISTS (
                    SELECT 1 FROM folders p WHERE p.id = f.parent_id AND p.is_deleted = false
                  ))

                UNION ALL

                SELECT d.id
                FROM documents d
                WHERE d.created_by = :userId AND d.is_deleted = true
                  AND (d.folder_id IS NULL OR EXISTS (
                    SELECT 1 FROM folders p WHERE p.id = d.folder_id AND p.is_deleted = false
                  ))
            ) AS total
            """, nativeQuery = true)
    Page<FileItemProjection> findTrashFiles(@Param("userId") UUID userId, Pageable pageable);

    @Query(value = """
            SELECT f.id AS id, f.name AS name, 'folder' AS type,
                   f.created_at AS createdAt, f.updated_at AS updatedAt, f.is_deleted AS isDeleted,
                   u.id AS createdById, u.email AS createdByEmail, u.first_name AS createdByFirstName, u.last_name AS createdByLastName,
                   NULL as description, 'OWNER' as permission, NULL as mime_type, NULL as processingStatus, NULL as fileSize
            FROM folders f
            JOIN users u ON f.created_by = u.id
            WHERE f.created_by = :userId AND f.is_deleted = :deleted

            UNION ALL

            SELECT d.id AS id, d.name AS name, 'document' AS type,
                   d.created_at AS createdAt, d.updated_at AS updatedAt, d.is_deleted AS isDeleted,
                   u.id AS createdById, u.email AS createdByEmail, u.first_name AS createdByFirstName, u.last_name AS createdByLastName,
                   d.description as description, 'OWNER' as permission,  d.mime_type as mime_type, d.processing_status as processingStatus, d.file_size as fileSize
            FROM documents d
            JOIN users u ON d.created_by = u.id
            WHERE d.created_by = :userId AND d.is_deleted = :deleted

            ORDER BY updatedAt DESC NULLS LAST, createdAt DESC
            """, countQuery = """
            SELECT COUNT(*) FROM (
                SELECT f.id
                FROM folders f
                WHERE f.created_by = :userId AND f.is_deleted = :deleted

                UNION ALL

                SELECT d.id
                FROM documents d
                WHERE d.created_by = :userId AND d.is_deleted = :deleted
            ) AS total
            """, nativeQuery = true)
    Page<FileItemProjection> findRecentFiles(@Param("userId") UUID userId,
            @Param("deleted") Boolean deleted,
            Pageable pageable);

    @Query(value = "SELECT * FROM (" + """
            SELECT f.id AS id, f.name AS name, 'folder' AS type,
                   f.created_at AS createdAt, f.updated_at AS updatedAt, f.is_deleted AS isDeleted,
                   u.id AS createdById, u.email AS createdByEmail, u.first_name AS createdByFirstName, u.last_name AS createdByLastName,  NULL as mime_type,
                   NULL as description,
                   CASE
                       WHEN f.created_by = :userId THEN 'OWNER'
                       ELSE fs.share_type
                   END as permission,
                   NULL as processingStatus, NULL as fileSize,
                   0 as sortType
            FROM folders f
            JOIN users u ON f.created_by = u.id
            LEFT JOIN folder_shares fs ON f.id = fs.folder_id AND fs.user_id = :userId
            WHERE f.is_deleted = :deleted AND f.parent_id = :folderId
                AND (f.created_by = :userId OR fs.user_id IS NOT NULL)
            """ + BROWSE_FOLDER_FILTER + """


            UNION ALL

            SELECT d.id AS id, d.name AS name, 'document' AS type,
                   d.created_at AS createdAt, d.updated_at AS updatedAt, d.is_deleted AS isDeleted,
                   u.id AS createdById, u.email AS createdByEmail, u.first_name AS createdByFirstName, u.last_name AS createdByLastName,  d.mime_type as mime_type,
                   d.description as description,
                   CASE
                       WHEN d.created_by = :userId THEN 'OWNER'
                       ELSE ds.share_type
                   END as permission,
                   d.processing_status as processingStatus, d.file_size as fileSize,
                   1 as sortType
            FROM documents d
            JOIN users u ON d.created_by = u.id
            LEFT JOIN document_shares ds ON d.id = ds.document_id AND ds.user_id = :userId
            WHERE d.is_deleted = :deleted AND d.folder_id = :folderId
                  AND (d.created_by = :userId OR ds.user_id IS NOT NULL)
            """ + BROWSE_DOCUMENT_FILTER + ") t " + BROWSE_ORDER, countQuery = """
            SELECT COUNT(*) FROM (
                SELECT f.id
                FROM folders f
                LEFT JOIN folder_shares fs ON f.id = fs.folder_id AND fs.user_id = :userId
                WHERE f.is_deleted = :deleted AND f.parent_id = :folderId
                      AND (f.created_by = :userId OR fs.user_id IS NOT NULL)
                """ + BROWSE_FOLDER_FILTER + """


                UNION ALL

                SELECT d.id
                FROM documents d
                LEFT JOIN document_shares ds ON d.id = ds.document_id AND ds.user_id = :userId
                WHERE d.is_deleted = :deleted AND d.folder_id = :folderId
                      AND (d.created_by = :userId OR ds.user_id IS NOT NULL)
                """ + BROWSE_DOCUMENT_FILTER + """
            ) AS total
            """, nativeQuery = true)
    Page<FileItemProjection> findFolderFiles(@Param("includeFolders") Boolean includeFolders,
            @Param("includeDocuments") Boolean includeDocuments,
            @Param("mimeType") String mimeType,
            @Param("updatedFrom") Instant updatedFrom,
            @Param("sortBy") String sortBy,
            @Param("sortDir") String sortDir,
            @Param("userId") UUID userId,
            @Param("folderId") UUID folderId,
            @Param("deleted") Boolean deleted,
            Pageable pageable);

    @Query(value = "SELECT * FROM (" + """
            SELECT f.id AS id, f.name AS name, 'folder' AS type,
                   f.created_at AS createdAt, f.updated_at AS updatedAt, f.is_deleted AS isDeleted,
                   u.id AS createdById, u.email AS createdByEmail, u.first_name AS createdByFirstName, u.last_name AS createdByLastName,
                   NULL as description, fs.share_type as permission, NULL as mime_type, NULL as processingStatus, NULL as fileSize,
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
            """ + BROWSE_FOLDER_FILTER + """


            UNION ALL

            SELECT d.id AS id, d.name AS name, 'document' AS type,
                   d.created_at AS createdAt, d.updated_at AS updatedAt, d.is_deleted AS isDeleted,
                   u.id AS createdById, u.email AS createdByEmail, u.first_name AS createdByFirstName, u.last_name AS createdByLastName,
                   d.description as description, ds.share_type as permission, d.mime_type as mime_type, d.processing_status as processingStatus, d.file_size as fileSize,
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
            """ + BROWSE_DOCUMENT_FILTER + ") t " + BROWSE_ORDER, countQuery = """
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
                """ + BROWSE_FOLDER_FILTER + """


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
                """ + BROWSE_DOCUMENT_FILTER + """
                ) AS total
            """, nativeQuery = true)
    Page<FileItemProjection> findSharedFiles(@Param("includeFolders") Boolean includeFolders,
            @Param("includeDocuments") Boolean includeDocuments,
            @Param("mimeType") String mimeType,
            @Param("updatedFrom") Instant updatedFrom,
            @Param("sortBy") String sortBy,
            @Param("sortDir") String sortDir,
            @Param("userId") UUID userId, Pageable pageable);

    @Query(value = """
            SELECT d.id AS id, d.name AS name, 'document' AS type,
                   d.created_at AS createdAt, d.updated_at AS updatedAt, d.is_deleted AS isDeleted,
                   u.id AS createdById, u.email AS createdByEmail, u.first_name AS createdByFirstName, u.last_name AS createdByLastName,
                   d.description as description, 'OWNER' as permission, d.mime_type as mime_type, d.processing_status as processingStatus, d.file_size as fileSize
            FROM documents d
            JOIN users u ON d.created_by = u.id
            WHERE d.created_by = :userId
              AND d.is_deleted = false
              AND (:keyword IS NULL OR :keyword = '' OR (d.name ILIKE '%' || :keyword || '%' ESCAPE '\\' OR (d.description IS NOT NULL AND d.description ILIKE '%' || :keyword || '%' ESCAPE '\\')))
              AND (:mimeType IS NULL OR d.mime_type LIKE CAST(:mimeType AS text))
              AND (:sizeType IS NULL OR (:sizeType = 'minSize' AND d.file_size >= :size)
                  OR (:sizeType = 'maxSize' AND d.file_size <= :size))
            ORDER BY d.name ASC
            """, countQuery = """
            SELECT COUNT(*)
            FROM documents d
            WHERE d.created_by = :userId
              AND d.is_deleted = false
              AND (:keyword IS NULL OR :keyword = '' OR (d.name ILIKE '%' || :keyword || '%' ESCAPE '\\' OR (d.description IS NOT NULL AND d.description ILIKE '%' || :keyword || '%' ESCAPE '\\')))
              AND (:mimeType IS NULL OR d.mime_type LIKE CAST(:mimeType AS text))
              AND (:sizeType IS NULL OR (:sizeType = 'minSize' AND d.file_size >= :size)
                OR (:sizeType = 'maxSize' AND d.file_size <= :size))
            """, nativeQuery = true)
    Page<FileItemProjection> findExactDocs(@Param("userId") UUID userId,
            @Param("keyword") String keyword,
            @Param("mimeType") String mimeType,
            @Param("size") Double size,
            @Param("sizeType") String sizeType,
            Pageable pageable);

    @Query(value = """
            SELECT d.id AS id, d.name AS name, 'document' AS type,
                   d.created_at AS createdAt, d.updated_at AS updatedAt, d.is_deleted AS isDeleted,
                   u.id AS createdById, u.email AS createdByEmail, u.first_name AS createdByFirstName, u.last_name AS createdByLastName,
                   d.description AS description, 'OWNER' AS permission, d.mime_type AS mime_type, d.processing_status AS processingStatus, d.file_size AS fileSize
            FROM documents d
            JOIN users u ON d.created_by = u.id
            WHERE d.created_by = :userId AND d.is_deleted = false
              AND d.id IN (:ids)
            """, nativeQuery = true)
    List<FileItemProjection> findOwnedDocumentsByIds(@Param("userId") UUID userId,
            @Param("ids") List<UUID> ids);

}
