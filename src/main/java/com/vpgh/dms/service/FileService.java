package com.vpgh.dms.service;

import com.vpgh.dms.model.dto.response.FileItemDTO;
import com.vpgh.dms.model.entity.User;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface FileService {
    Page<FileItemDTO> getUserFiles(User user, Integer parentId, Map<String, String> params);

    Page<FileItemDTO> getTrashFiles(User user, Map<String, String> params);

    List<FileItemDTO> getAllTrashFiles(User user);

    Page<FileItemDTO> getRecentFiles(User user, Map<String, String> params);

    Page<FileItemDTO> getFolderFiles(User user, Integer folderId, Map<String, String> params);

    Page<FileItemDTO> getSharedFiles(User user, Map<String, String> params);

    Page<FileItemDTO> getAdvancedSearchFiles(User user, Map<String, String> params);
}
