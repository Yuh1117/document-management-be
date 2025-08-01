package com.vpgh.dms.service;

import com.vpgh.dms.model.entity.Folder;
import org.springframework.stereotype.Service;

@Service
public interface FolderService {
    Folder getFolderById(Integer id);
}
