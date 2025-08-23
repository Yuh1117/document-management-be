package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.dto.*;
import com.vpgh.dms.model.dto.response.FileItemDTO;
import com.vpgh.dms.model.dto.response.FileItemProjection;
import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.Folder;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.repository.FileItemRepository;
import com.vpgh.dms.service.*;
import com.vpgh.dms.util.PageSize;
import com.vpgh.dms.util.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class FileServiceImpl implements FileService {
    private final FileItemRepository fileItemRepository;
    private final FolderShareService folderShareService;
    private final DocumentShareService documentShareService;
    private final FolderService folderService;
    private final DocumentService documentService;

    public FileServiceImpl(FileItemRepository fileItemRepository, FolderShareService folderShareService,
                           DocumentShareService documentShareService, FolderService folderService, DocumentService documentService) {
        this.fileItemRepository = fileItemRepository;
        this.folderShareService = folderShareService;
        this.documentShareService = documentShareService;
        this.folderService = folderService;
        this.documentService = documentService;
    }

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

    @Override
    public Page<FileItemDTO> getSharedFiles(User user, Map<String, String> params) {
        Pageable pageable = Pageable.unpaged();
        if (params != null) {
            int page = Integer.parseInt(params.get("page"));
            pageable = PageRequest.of(page - 1, PageSize.FOLDER_PAGE_SIZE.getSize());
        }
        Page<FileItemProjection> pageItem = fileItemRepository.findSharedFiles(user.getId(), pageable);
        Page<FileItemDTO> items = pageItem.map(p -> mapToFileItemDTO(p));
        return items;
    }

    private FileItemDTO mapToFileItemDTO(FileItemProjection p) {
        UserDTO createdBy = new UserDTO();
        createdBy.setId(p.getCreatedById());
        createdBy.setEmail(p.getCreatedByEmail());

        FileItemDTO dto = new FileItemDTO();
        dto.setType(p.getType());

        String permission = "";
        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();
        if ("folder".equals(p.getType())) {
            FolderDTO f = new FolderDTO();
            f.setId(p.getId());
            f.setName(p.getName());
            f.setCreatedAt(p.getCreatedAt());
            f.setUpdatedAt(p.getUpdatedAt());
            f.setCreatedBy(createdBy);
            f.setDeleted(p.getIsDeleted());
            dto.setFolder(f);
            Folder folder = this.folderService.getFolderById(p.getId());
            if (this.folderService.isOwnerFolder(folder, currentUser)) {
                permission = "OWNER";
            } else if (this.folderShareService.checkCanEdit(currentUser, folder)) {
                permission = "EDIT";
            } else if (this.folderShareService.checkCanView(currentUser, folder)) {
                permission = "VIEW";
            }

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
            Document doc = this.documentService.getDocumentById(p.getId());
            if (this.documentService.isOwnerDoc(doc, currentUser)) {
                permission = "OWNER";
            } else if (this.documentShareService.checkCanEdit(currentUser, doc)) {
                permission = "EDIT";
            } else if (this.documentShareService.checkCanView(currentUser, doc)) {
                permission = "VIEW";
            }
        }
        dto.setPermission(permission);
        return dto;
    }
}
