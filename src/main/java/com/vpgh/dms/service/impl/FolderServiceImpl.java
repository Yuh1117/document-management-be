package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.entity.Folder;
import com.vpgh.dms.repository.FolderRepository;
import com.vpgh.dms.service.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FolderServiceImpl implements FolderService {

    @Autowired
    private FolderRepository folderRepository;

    @Override
    public Folder getFolderById(Integer id) {
        Optional<Folder> folder = this.folderRepository.findById(id);
        return folder.isPresent() ? folder.get() : null;
    }
}
