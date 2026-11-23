package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.dto.*;
import com.vpgh.dms.model.dto.processor.ProcessorSearchRequest;
import com.vpgh.dms.model.dto.processor.ProcessorSearchResponse;
import com.vpgh.dms.model.dto.response.FileItemDTO;
import com.vpgh.dms.model.dto.response.FileItemProjection;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.repository.DocumentRepository;
import com.vpgh.dms.repository.FileRepository;
import com.vpgh.dms.service.FileService;
import com.vpgh.dms.service.ProcessorSearchService;
import com.vpgh.dms.util.PageSize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements FileService {
    private final FileRepository fileRepository;
    private final ProcessorSearchService processorSearchService;
    private final DocumentRepository documentRepository;

    public FileServiceImpl(FileRepository fileRepository, ProcessorSearchService processorSearchService,
            DocumentRepository documentRepository) {
        this.fileRepository = fileRepository;
        this.processorSearchService = processorSearchService;
        this.documentRepository = documentRepository;
    }

    @Override
    public Page<FileItemDTO> getUserFiles(User user, boolean matchAll, Map<String, String> params) {
        Pageable pageable = Pageable.unpaged();
        String keyword = null;
        if (params != null) {
            if (params.containsKey("page")) {
                int page = Integer.parseInt(params.get("page"));
                pageable = PageRequest.of(page - 1, PageSize.FOLDER_PAGE_SIZE.getSize());
            }

            keyword = escapeLikeKeyword(params.get("kw"));
        }

        BrowseOptions o = parseBrowseOptions(params);
        Page<FileItemProjection> pageItem = fileRepository.findAllByUserAndParent(o.includeFolders(),
                o.includeDocuments(), o.mimeType(), o.updatedFrom(), o.sortBy(), o.sortDir(),
                user.getId(), null, matchAll, false, keyword, pageable);
        return pageItem.map(this::mapToFileItemDTO);
    }

    @Override
    public Page<FileItemDTO> getTrashFiles(User user, Map<String, String> params) {
        Pageable pageable = Pageable.unpaged();
        if (params != null && params.containsKey("page")) {
            int page = Integer.parseInt(params.get("page"));
            pageable = PageRequest.of(page - 1, PageSize.FOLDER_PAGE_SIZE.getSize());
        }
        Page<FileItemProjection> pageItem = fileRepository.findTrashFiles(user.getId(), pageable);
        return pageItem.map(this::mapToFileItemDTO);
    }

    @Override
    public List<FileItemDTO> getAllTrashFiles(User user) {
        return fileRepository.findTrashFiles(user.getId(), Pageable.unpaged())
                .stream()
                .map(this::mapToFileItemDTO)
                .toList();
    }

    @Override
    public Page<FileItemDTO> getRecentFiles(User user, Map<String, String> params) {
        Pageable pageable = Pageable.unpaged();
        if (params != null && params.containsKey("page")) {
            int page = Integer.parseInt(params.get("page"));
            pageable = PageRequest.of(page - 1, PageSize.FOLDER_PAGE_SIZE.getSize());
        }
        Page<FileItemProjection> pageItem = fileRepository.findRecentFiles(user.getId(), false, pageable);
        return pageItem.map(this::mapToFileItemDTO);
    }

    @Override
    public Page<FileItemDTO> getFolderFiles(User user, UUID folderId, Map<String, String> params) {
        Pageable pageable = Pageable.unpaged();
        if (params != null && params.containsKey("page")) {
            int page = Integer.parseInt(params.get("page"));
            pageable = PageRequest.of(page - 1, PageSize.FOLDER_PAGE_SIZE.getSize());
        }

        BrowseOptions o = parseBrowseOptions(params);
        Page<FileItemProjection> pageItem = fileRepository.findFolderFiles(o.includeFolders(),
                o.includeDocuments(), o.mimeType(), o.updatedFrom(), o.sortBy(), o.sortDir(),
                user.getId(), folderId, false, pageable);
        return pageItem.map(this::mapToFileItemDTO);
    }

    @Override
    public Page<FileItemDTO> getSharedFiles(User user, Map<String, String> params) {
        Pageable pageable = Pageable.unpaged();
        if (params != null && params.containsKey("page")) {
            int page = Integer.parseInt(params.get("page"));
            pageable = PageRequest.of(page - 1, PageSize.FOLDER_PAGE_SIZE.getSize());
        }
        BrowseOptions o = parseBrowseOptions(params);
        Page<FileItemProjection> pageItem = fileRepository.findSharedFiles(o.includeFolders(),
                o.includeDocuments(), o.mimeType(), o.updatedFrom(), o.sortBy(), o.sortDir(),
                user.getId(), pageable);
        return pageItem.map(this::mapToFileItemDTO);
    }

    @Override
    public Page<FileItemDTO> getAdvancedSearchFiles(User user, Map<String, String> params) {
        if (params != null) {
            String kwType = params.get("kwType");
            if (kwType != null && !kwType.isBlank()) {
                String normalized = kwType.trim().toLowerCase(Locale.ROOT);
                if ("full_text".equals(normalized)) {
                    return searchViaElasticsearch(user, params, "full_text");
                }
                if ("semantic".equals(normalized)) {
                    return searchViaElasticsearch(user, params, "semantic");
                }
                if ("hybrid".equals(normalized)) {
                    return searchViaElasticsearch(user, params, "hybrid");
                }
            }
        }

        return searchExactInDatabase(user, params);
    }

    private Page<FileItemDTO> searchViaElasticsearch(User user, Map<String, String> params, String mode) {
        int page = parsePositiveInt(params.get("page"), 1);
        int pageSize = Math.min(parsePositiveInt(params.get("page_size"), PageSize.FOLDER_PAGE_SIZE.getSize()), 100);

        String rawKeyword = params.get("kw");
        String searchKeyword = (rawKeyword != null && !rawKeyword.isBlank()) ? rawKeyword.trim() : null;
        if (searchKeyword == null || searchKeyword.isEmpty()) {
            return new PageImpl<>(List.of(), PageRequest.of(page - 1, pageSize), 0);
        }

        MimeSizeFilters filters = parseMimeSizeFilters(params);
        Long sizeBytes = filters.sizeBytes() != null ? filters.sizeBytes().longValue() : null;
        ProcessorSearchRequest request = new ProcessorSearchRequest(searchKeyword, user.getId(), page,
                pageSize, mode,
                filters.mimeType(),
                "minSize".equals(filters.sizeType()) ? sizeBytes : null,
                "maxSize".equals(filters.sizeType()) ? sizeBytes : null,
                documentRepository.findDeletedDocumentIdsByOwner(user.getId()));
        ProcessorSearchResponse response = processorSearchService.search(request);
        List<ProcessorSearchResponse.ProcessorSearchHit> hits = response.getHits() != null ? response.getHits()
                : List.of();

        List<UUID> orderedDocIds = hits.stream()
                .map(hit -> parseDocumentId(hit.getDocumentId()))
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Pageable pageable = PageRequest.of(page - 1, pageSize);
        if (orderedDocIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, response.getTotal());
        }

        List<FileItemProjection> rows = fileRepository.findOwnedDocumentsByIds(user.getId(), orderedDocIds);
        Map<UUID, FileItemProjection> rowMap = rows.stream().collect(Collectors.toMap(r -> r.getId(), r -> r));
        Map<UUID, String> snippetMap = new HashMap<>();
        for (ProcessorSearchResponse.ProcessorSearchHit hit : hits) {
            UUID docId = parseDocumentId(hit.getDocumentId());
            if (docId != null && hit.getSnippet() != null && !snippetMap.containsKey(docId)) {
                snippetMap.put(docId, hit.getSnippet());
            }
        }
        List<FileItemDTO> content = orderedDocIds.stream()
                .map(rowMap::get)
                .filter(Objects::nonNull)
                .map(row -> mapToFileItemDTO(row, snippetMap.get(row.getId())))
                .toList();

        return new PageImpl<>(content, pageable, response.getTotal());
    }

    private static int parsePositiveInt(String raw, int defaultValue) {
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            int v = Integer.parseInt(raw.trim());
            return v >= 1 ? v : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Page<FileItemDTO> searchExactInDatabase(User user, Map<String, String> params) {
        Pageable pageable = Pageable.unpaged();
        String rawKeyword = null;

        if (params != null) {
            if (params.containsKey("page")) {
                int page = Integer.parseInt(params.get("page"));
                pageable = PageRequest.of(page - 1, PageSize.FOLDER_PAGE_SIZE.getSize());
            }
            rawKeyword = params.get("kw");
        }

        MimeSizeFilters filters = parseMimeSizeFilters(params);
        String searchKeyword = escapeLikeKeyword(rawKeyword);
        Page<FileItemProjection> pageItem = fileRepository.findExactDocs(
                user.getId(),
                searchKeyword,
                filters.mimeType(),
                filters.sizeBytes(),
                filters.sizeType(),
                pageable);
        return pageItem.map(this::mapToFileItemDTO);
    }

    private record MimeSizeFilters(String mimeType, Double sizeBytes, String sizeType) {
    }

    private static MimeSizeFilters parseMimeSizeFilters(Map<String, String> params) {
        if (params == null) {
            return new MimeSizeFilters(null, null, null);
        }
        String mimeType = null;
        String typeStr = params.get("type");
        if (typeStr != null && !typeStr.isBlank() && !"any".equalsIgnoreCase(typeStr.trim())) {
            mimeType = mapMimeTypeStatic(typeStr.trim());
        }
        Double sizeBytes = null;
        String sizeType = null;
        String sizeTypeStr = params.get("sizeType");
        if (sizeTypeStr != null && !sizeTypeStr.isBlank()) {
            String sizeStr = params.get("size");
            if (sizeStr != null && !sizeStr.isBlank()) {
                try {
                    sizeBytes = Double.parseDouble(sizeStr.trim()) * 1024 * 1024;
                } catch (NumberFormatException ignored) {
                }
            }
            if (sizeBytes != null) {
                sizeType = sizeTypeStr.trim();
            }
        }
        return new MimeSizeFilters(mimeType, sizeBytes, sizeType);
    }

    private record BrowseOptions(boolean includeFolders, boolean includeDocuments, String mimeType,
            Instant updatedFrom, String sortBy, String sortDir) {
    }

    private static final Set<String> SORT_FIELDS = Set.of("name", "updatedAt", "size");

    /**
     * Parses the quick filter / sort params shared by the my-files, folder and shared views:
     * type (folder|pdf|word|image), modified (today|7d|30d|365d), sortBy (name|updatedAt|size), sortDir (asc|desc).
     */
    private static BrowseOptions parseBrowseOptions(Map<String, String> params) {
        Map<String, String> p = params != null ? params : Map.of();

        String type = trimToNull(p.get("type"));
        boolean includeFolders = type == null || "folder".equals(type);
        boolean includeDocuments = type == null || !"folder".equals(type);
        String mimeType = type != null && includeDocuments ? mapMimeTypeStatic(type) : null;

        Instant updatedFrom = Instant.EPOCH;
        String modified = trimToNull(p.get("modified"));
        if (modified != null) {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            updatedFrom = switch (modified) {
                case "today" -> now.truncatedTo(ChronoUnit.DAYS).toInstant();
                case "7d" -> now.minusDays(7).toInstant();
                case "30d" -> now.minusDays(30).toInstant();
                case "365d" -> now.minusDays(365).toInstant();
                default -> Instant.EPOCH;
            };
        }

        String sortBy = trimToNull(p.get("sortBy"));
        if (sortBy == null || !SORT_FIELDS.contains(sortBy)) {
            sortBy = "name";
        }
        String sortDir = "desc".equalsIgnoreCase(trimToNull(p.get("sortDir"))) ? "desc" : "asc";

        return new BrowseOptions(includeFolders, includeDocuments, mimeType, updatedFrom, sortBy, sortDir);
    }

    private static String trimToNull(String raw) {
        return raw == null || raw.isBlank() ? null : raw.trim();
    }

    private FileItemDTO mapToFileItemDTO(FileItemProjection p) {
        return mapToFileItemDTO(p, null);
    }

    private static UUID parseDocumentId(String rawDocumentId) {
        if (rawDocumentId == null || rawDocumentId.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(rawDocumentId.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private FileItemDTO mapToFileItemDTO(FileItemProjection p, String snippet) {
        UserDTO createdBy = new UserDTO();
        createdBy.setId(p.getCreatedById());
        createdBy.setEmail(p.getCreatedByEmail());
        createdBy.setFirstName(p.getCreatedByFirstName());
        createdBy.setLastName(p.getCreatedByLastName());

        FileItemDTO dto = new FileItemDTO();
        dto.setType(p.getType());

        if ("folder".equals(p.getType())) {
            FolderDTO f = new FolderDTO();
            f.setId(p.getId());
            f.setName(p.getName());
            f.setCreatedAt(p.getCreatedAt());
            f.setUpdatedAt(p.getUpdatedAt());
            f.setCreatedBy(createdBy);
            f.setDeleted(p.getIsDeleted());
            dto.setFolder(f);
        } else {
            DocumentDTO d = new DocumentDTO();
            d.setId(p.getId());
            d.setName(p.getName());
            d.setDescription(p.getDescription());
            d.setMimeType(p.getMimeType());
            d.setFileSize(p.getFileSize());
            d.setProcessingStatus(p.getProcessingStatus());
            d.setCreatedAt(p.getCreatedAt());
            d.setUpdatedAt(p.getUpdatedAt());
            d.setCreatedBy(createdBy);
            d.setDeleted(p.getIsDeleted());
            d.setSnippet(snippet);
            dto.setDocument(d);
        }

        dto.setPermission(p.getPermission());
        return dto;
    }

    private static String escapeLikeKeyword(String raw) {
        if (raw == null || raw.isBlank())
            return null;
        return raw.trim()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    private static String mapMimeTypeStatic(String type) {
        return switch (type) {
            case "pdf" -> "application/pdf";
            case "word" -> "%word%";
            case "image" -> "image/%";
            default -> null;
        };
    }

}
