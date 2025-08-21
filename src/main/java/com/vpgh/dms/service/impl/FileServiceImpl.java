package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.dto.*;
import com.vpgh.dms.model.dto.response.FileItemDTO;
import com.vpgh.dms.model.dto.response.FileItemProjection;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.repository.FileItemRepository;
import com.vpgh.dms.service.FileService;
import com.vpgh.dms.util.PageSize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class FileServiceImpl implements FileService {
    @Autowired
    private FileItemRepository fileItemRepository;

    @Override
    public Page<FileItemDTO> getUserFiles(User user, Integer parentId, Map<String, String> params) {
        Pageable pageable = Pageable.unpaged();
        String keyword = null;
        if (params != null) {
            int page = Integer.parseInt(params.get("page"));
            pageable = PageRequest.of(page - 1, PageSize.FOLDER_PAGE_SIZE.getSize());

            keyword = params.get("kw");
            if (keyword != null && keyword.trim().isEmpty()) {
                keyword = null;
            }
        }

        Page<FileItemProjection> pageItem = fileItemRepository.findAllByUserAndParent(user.getId(), parentId, false, keyword, pageable);
        Page<FileItemDTO> items = pageItem.map(p -> mapToFileItemDTO(p));
        return items;
    }

    @Override
    public Page<FileItemDTO> getTrashFiles(User user, Map<String, String> params) {
        Pageable pageable = Pageable.unpaged();
        if (params != null) {
            int page = Integer.parseInt(params.get("page"));
            pageable = PageRequest.of(page - 1, PageSize.FOLDER_PAGE_SIZE.getSize());
        }
        Page<FileItemProjection> pageItem = fileItemRepository.findTrashFiles(user.getId(), pageable);
        Page<FileItemDTO> items = pageItem.map(p -> mapToFileItemDTO(p));
        return items;
    }

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
    public Page<FileItemDTO> getFolderFiles(Integer folderId, Map<String, String> params) {
        Pageable pageable = Pageable.unpaged();
        if (params != null) {
            int page = Integer.parseInt(params.get("page"));
            pageable = PageRequest.of(page - 1, PageSize.FOLDER_PAGE_SIZE.getSize());
        }

        Page<FileItemProjection> pageItem = fileItemRepository.findFolderFiles(folderId, false, pageable);
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
            d.setCreatedAt(p.getCreatedAt());
            d.setUpdatedAt(p.getUpdatedAt());
            d.setCreatedBy(createdBy);
            d.setDeleted(p.getIsDeleted());
            dto.setDocument(d);
        }
        return dto;
    }
}
