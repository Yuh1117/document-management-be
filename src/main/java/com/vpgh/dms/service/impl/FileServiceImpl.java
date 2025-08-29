package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.dto.*;
import com.vpgh.dms.model.dto.response.FileItemDTO;
import com.vpgh.dms.model.dto.response.FileItemProjection;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.repository.FileRepository;
import com.vpgh.dms.service.*;
import com.vpgh.dms.util.PageSize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements FileService {
    private final FileRepository fileRepository;
    private final EmbeddingService embeddingService;


    public FileServiceImpl(FileRepository fileRepository, EmbeddingService embeddingService) {
        this.fileRepository = fileRepository;
        this.embeddingService = embeddingService;
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

        Page<FileItemProjection> pageItem = fileRepository.findAllByUserAndParent(user.getId(), parentId, false, keyword, pageable);
        Page<FileItemDTO> items = pageItem.map(p -> mapToFileItemDTO(p));
        return items;
    }

    @Override
    public Page<FileItemDTO> getTrashFiles(User user, Map<String, String> params) {
        Pageable pageable = Pageable.unpaged();
        if (params != null && params.containsKey("page")) {
            int page = Integer.parseInt(params.get("page"));
            pageable = PageRequest.of(page - 1, PageSize.FOLDER_PAGE_SIZE.getSize());
        }
        Page<FileItemProjection> pageItem = fileRepository.findTrashFiles(user.getId(), pageable);
        Page<FileItemDTO> items = pageItem.map(p -> mapToFileItemDTO(p));
        return items;
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
        Page<FileItemDTO> items = pageItem.map(p -> mapToFileItemDTO(p));
        return items;
    }

    @Override
    public Page<FileItemDTO> getFolderFiles(User user, Integer folderId, Map<String, String> params) {
        Pageable pageable = Pageable.unpaged();
        if (params != null && params.containsKey("page")) {
            int page = Integer.parseInt(params.get("page"));
            pageable = PageRequest.of(page - 1, PageSize.FOLDER_PAGE_SIZE.getSize());
        }

        Page<FileItemProjection> pageItem = fileRepository.findFolderFiles(user.getId(), folderId, false, pageable);
        Page<FileItemDTO> items = pageItem.map(p -> mapToFileItemDTO(p));
        return items;
    }

    @Override
    public Page<FileItemDTO> getSharedFiles(User user, Map<String, String> params) {
        Pageable pageable = Pageable.unpaged();
        if (params != null && params.containsKey("page")) {
            int page = Integer.parseInt(params.get("page"));
            pageable = PageRequest.of(page - 1, PageSize.FOLDER_PAGE_SIZE.getSize());
        }
        Page<FileItemProjection> pageItem = fileRepository.findSharedFiles(user.getId(), pageable);
        Page<FileItemDTO> items = pageItem.map(p -> mapToFileItemDTO(p));
        return items;
    }

    @Override
    public Page<FileItemDTO> getAdvancedSearchFiles(User user, Map<String, String> params) {
        Pageable pageable = Pageable.unpaged();
        String rawKeyword = null;
        String keyword = null;
        String kwType = null;
        String mimeType = null;
        Double size = null;
        String sizeType = null;

        if (params != null) {
            if (params.containsKey("page")) {
                int page = Integer.parseInt(params.get("page"));
                pageable = PageRequest.of(page - 1, PageSize.FOLDER_PAGE_SIZE.getSize());
            }

            String kwTypeStr = params.get("kwType");
            if (kwTypeStr != null && !kwTypeStr.isBlank()) {
                kwType = kwTypeStr;
                rawKeyword = params.getOrDefault("kw", null);
                if (rawKeyword != null && !rawKeyword.isBlank()) {
                    keyword = Arrays.stream(rawKeyword.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(s -> s.contains(" ") ? s.replaceAll("\\s+", " & ") : s)
                            .collect(Collectors.joining(" | "));
                }
            }

            String mimeTypeStr = params.get("type");
            if (mimeTypeStr != null && !mimeTypeStr.isBlank()) {
                mimeType = mapMimeType(mimeTypeStr);
            }

            String sizeTypeStr = params.get("sizeType");
            if (sizeTypeStr != null && !sizeTypeStr.isBlank()) {
                sizeType = sizeTypeStr;
                String sizeStr = params.get("size");
                if (sizeStr != null && !sizeStr.isBlank()) {
                    try {
                        size = Double.parseDouble(sizeStr) * 1024 * 1024;
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

        }

        Page<FileItemProjection> pageItem = null;
        if (keyword == null || "exact".equals(kwType)) {
            pageItem = fileRepository.findExactDocs(user.getId(), keyword, mimeType, size, sizeType, pageable);
        } else {
            List<Double> embedding = embeddingService.getEmbedding(rawKeyword);
            float[] embeddingArray = new float[embedding.size()];
            for (int i = 0; i < embedding.size(); i++) {
                embeddingArray[i] = embedding.get(i).floatValue();
            }
            pageItem = fileRepository.findSimilarDocs(user.getId(), embeddingArray, 0.7, mimeType, size, sizeType, pageable);
        }
        Page<FileItemDTO> items = pageItem.map(p -> mapToFileItemDTO(p));
        return items;
    }

    private FileItemDTO mapToFileItemDTO(FileItemProjection p) {
        UserDTO createdBy = new UserDTO();
        createdBy.setId(p.getCreatedById());
        createdBy.setEmail(p.getCreatedByEmail());

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

    private String mapMimeType(String type) {
        if (type == null || type.equals("any")) {
            return null;
        }
        return switch (type) {
            case "pdf" -> "application/pdf";
            case "word" -> "%word%";
            case "image" -> "image/%";
            default -> null;
        };
    }
}
