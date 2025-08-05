package com.vpgh.dms.service;

import com.vpgh.dms.model.entity.Folder;
import com.vpgh.dms.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface FolderService {
    Folder getFolderById(Integer id);

    boolean existsByNameAndParentAndIdNot(String name, Folder parent, Integer excludeId);

    Folder save(Folder folder);

    void softDeleteFolderAndChildren(Folder folder);

    void restoreFolderAndChildren(Folder folder);

    void hardDeleteFolderAndChildren(Folder folder);

    Page<Folder> getActiveFolders(Folder parent, User createdBy, String page);

    Page<Folder> getInactiveFolders(Folder parent, User createdBy, String page);

    Page<Folder> searchFolders(Map<String, String> params, User user);
}
