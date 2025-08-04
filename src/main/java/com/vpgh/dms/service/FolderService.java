package com.vpgh.dms.service;

import com.vpgh.dms.model.entity.Folder;
import org.springframework.stereotype.Service;

@Service
public interface FolderService {
    Folder getFolderById(Integer id);

    boolean existsByNameAndParentAndIdNot(String name, Folder parent, Integer excludeId);

    Folder save(Folder folder);

    void softDeleteFolderAndChildren(Folder folder);

    void restoreFolderAndChildren(Folder folder);

    void hardDeleteFolderAndChildren(Folder folder);

}
