package com.vpgh.dms.service;

import com.vpgh.dms.model.dto.FolderDTO;
import com.vpgh.dms.model.dto.SubFolderDTO;
import com.vpgh.dms.model.entity.Folder;
import com.vpgh.dms.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

@Service
public interface FolderService {
    Folder getFolderById(Integer id);

    List<Folder> getFoldersByIds(List<Integer> ids);

    boolean existsByNameAndParentAndIsDeletedFalseAndIdNot(String name, Folder parent, Integer id);

    boolean existsByNameAndCreatedByAndParentIsNullAndIsDeletedFalseAndIdNot(String name, User createdBy, Integer id);

    Folder save(Folder folder);

    void softDeleteFolderAndChildren(Folder folder);

    void restoreFolderAndChildren(Folder folder);

    void hardDeleteFolderAndChildren(Folder folder);

    Page<Folder> getActiveFolders(Folder parent, User createdBy, String page);

    Page<Folder> getInactiveFolders(Folder parent, User createdBy, String page);

    Page<Folder> searchFolders(Map<String, String> params, User user);

    List<Folder> findByParentAndIsDeletedFalse(Folder parent);

    List<Folder> findByParentAndIsDeletedTrue(Folder parent);

    void copyFolder(Folder folder, Folder targetFolder);

    void moveFolder(Folder folder, Folder targetFolder);

    FolderDTO convertFolderToFolderDTO(Folder folder);

    List<FolderDTO> convertFoldersToFolderDTOs(List<Folder> folders);

    SubFolderDTO convertFolderToSubFolderDTO(Folder folder);

    List<SubFolderDTO> convertFoldersToSubFolderDTOs(List<Folder> folders);

    void zipFolderIterative(Folder rootFolder, ZipOutputStream zipOut) throws IOException;

    Folder uploadNewFolder(Folder parentFolder, List<MultipartFile> files, List<String> relativePaths) throws IOException;
}
