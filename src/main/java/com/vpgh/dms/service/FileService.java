package com.vpgh.dms.service;

import com.vpgh.dms.model.dto.response.FileItemDTO;
import com.vpgh.dms.model.entity.User;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface FileService {
    Page<FileItemDTO> getUserFiles(User user, Integer parentId, Map<String, String> params);

    Page<FileItemDTO> getTrashFiles(User user, Integer parentId, Map<String, String> params);

    Page<FileItemDTO> getFolderFiles(Integer folderId, Map<String, String> params);
}
