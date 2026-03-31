package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.dto.*;
import com.vpgh.dms.model.dto.processor.ProcessorSearchRequest;
import com.vpgh.dms.model.dto.processor.ProcessorSearchResponse;
import com.vpgh.dms.model.dto.response.FileItemDTO;
import com.vpgh.dms.model.dto.response.FileItemProjection;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.repository.FileRepository;
import com.vpgh.dms.service.FileService;
import com.vpgh.dms.service.ProcessorSearchClient;
import com.vpgh.dms.util.PageSize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements FileService {
    private final FileRepository fileRepository;
    private final ProcessorSearchClient processorSearchClient;

    public FileServiceImpl(FileRepository fileRepository, ProcessorSearchClient processorSearchClient) {
        this.fileRepository = fileRepository;
        this.processorSearchClient = processorSearchClient;
    }

    @Override
    public Page<FileItemDTO> getUserFiles(User user, Integer parentId, Map<String, String> params) {
        Pageable pageable = Pageable.unpaged();
        String keyword = null;
        if (params != null) {
            if (params.containsKey("page")) {
                int page = Integer.parseInt(params.get("page"));
                pageable = PageRequest.of(page - 1, PageSize.FOLDER_PAGE_SIZE.getSize());
            }

            keyword = params.get("kw");
        }

        Page<FileItemProjection> pageItem = fileRepository.findAllByUserAndParent(user.getId(), parentId, false,
                keyword, pageable);
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
        Page<FileItemDTO> page = this.getTrashFiles(user, Map.of("page", "1"));
        List<FileItemDTO> allItems = new ArrayList<>(page.getContent());

        while (page.hasNext()) {
            page = this.getTrashFiles(user, Map.of("page", String.valueOf(page.getNumber() + 2)));
            allItems.addAll(page.getContent());
        }

        return allItems;
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
    public Page<FileItemDTO> getFolderFiles(User user, Integer folderId, Map<String, String> params) {
        Pageable pageable = Pageable.unpaged();
        if (params != null && params.containsKey("page")) {
            int page = Integer.parseInt(params.get("page"));
            pageable = PageRequest.of(page - 1, PageSize.FOLDER_PAGE_SIZE.getSize());
        }

        Page<FileItemProjection> pageItem = fileRepository.findFolderFiles(user.getId(), folderId, false, pageable);
        return pageItem.map(this::mapToFileItemDTO);
    }

    @Override
    public Page<FileItemDTO> getSharedFiles(User user, Map<String, String> params) {
        Pageable pageable = Pageable.unpaged();
        if (params != null && params.containsKey("page")) {
            int page = Integer.parseInt(params.get("page"));
            pageable = PageRequest.of(page - 1, PageSize.FOLDER_PAGE_SIZE.getSize());
        }
        Page<FileItemProjection> pageItem = fileRepository.findSharedFiles(user.getId(), pageable);
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

        Integer folderId = null;
        String folderIdStr = params.get("folderId");
        if (folderIdStr != null && !folderIdStr.isBlank()) {
            try {
                folderId = Integer.parseInt(folderIdStr.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        ProcessorSearchRequest request = new ProcessorSearchRequest(searchKeyword, user.getId(), folderId, page,
                pageSize, mode);
        ProcessorSearchResponse response = processorSearchClient.search(request);
        List<ProcessorSearchResponse.ProcessorSearchHit> hits = response.getHits() != null ? response.getHits()
                : List.of();

        List<Integer> orderedDocIds = hits.stream()
                .map(hit -> {
                    try {
                        return hit.getDocumentId() != null ? Integer.parseInt(hit.getDocumentId().trim()) : null;
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Pageable pageable = PageRequest.of(page - 1, pageSize);
        if (orderedDocIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, response.getTotal());
        }

        MimeSizeFilters filters = parseMimeSizeFilters(params);
        List<FileItemProjection> rows = fileRepository.findOwnedDocumentsByIdsAndFilters(user.getId(),
                orderedDocIds, filters.mimeType(), filters.sizeBytes(), filters.sizeType());
        Map<Integer, FileItemProjection> rowMap = rows.stream().collect(Collectors.toMap(r -> r.getId(), r -> r));
        List<FileItemDTO> content = orderedDocIds.stream()
                .map(rowMap::get)
                .filter(Objects::nonNull)
                .map(this::mapToFileItemDTO)
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
        String searchKeyword = (rawKeyword != null && !rawKeyword.isBlank()) ? rawKeyword.trim() : null;
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
            sizeType = sizeTypeStr.trim();
            String sizeStr = params.get("size");
            if (sizeStr != null && !sizeStr.isBlank()) {
                try {
                    sizeBytes = Double.parseDouble(sizeStr.trim()) * 1024 * 1024;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return new MimeSizeFilters(mimeType, sizeBytes, sizeType);
    }

    private FileItemDTO mapToFileItemDTO(FileItemProjection p) {
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
            d.setCreatedAt(p.getCreatedAt());
            d.setUpdatedAt(p.getUpdatedAt());
            d.setCreatedBy(createdBy);
            d.setDeleted(p.getIsDeleted());
            dto.setDocument(d);
        }

        dto.setPermission(p.getPermission());
        return dto;
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
