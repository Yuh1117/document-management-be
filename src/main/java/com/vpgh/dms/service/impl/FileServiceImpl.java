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

import java.util.Map;

@Service
public class FileServiceImpl implements FileService {
    @Autowired
    private FileItemRepository fileItemRepository;

    @Override
    public Page<FileItemDTO> getUserFiles(User user, Integer parentId, Map<String, String> params) {
        int page = Integer.parseInt(params.get("page"));
        Pageable pageable = PageRequest.of(page - 1, PageSize.FOLDER_PAGE_SIZE.getSize());

        Page<FileItemProjection> pageItem = fileItemRepository.findAllByUserAndParent(user.getId(), parentId, pageable);

        Page<FileItemDTO> items = pageItem.map(p -> {
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
                dto.setFolder(f);
            } else {
                DocumentDTO d = new DocumentDTO();
                d.setId(p.getId());
                d.setName(p.getName());
                d.setCreatedAt(p.getCreatedAt());
                d.setUpdatedAt(p.getUpdatedAt());
                d.setCreatedBy(createdBy);
                dto.setDocument(d);
            }
            return dto;
        });

        return items;
    }
}
