package com.vpgh.dms.service;

import com.vpgh.dms.model.dto.FolderDTO;
import com.vpgh.dms.model.dto.SubFolderDTO;
import com.vpgh.dms.model.entity.Folder;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.model.dto.response.FolderUploadPlan;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
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

    List<Folder> findByParentAndIsDeletedFalse(Folder parent);

    List<Folder> findByParentAndIsDeletedTrue(Folder parent);

    void copyFolder(Folder folder, Folder targetFolder);

    void moveFolder(Folder folder, Folder targetFolder);

    boolean isDescendant(Folder source, Folder target);

    FolderDTO convertFolderToFolderDTO(Folder folder);

    List<FolderDTO> convertFoldersToFolderDTOs(List<Folder> folders);

    SubFolderDTO convertFolderToSubFolderDTO(Folder folder);

    List<SubFolderDTO> convertFoldersToSubFolderDTOs(List<Folder> folders);

    void zipFolderIterative(Folder rootFolder, ZipOutputStream zipOut) throws IOException;

    FolderUploadPlan buildFolderStructure(Folder parentFolder, List<String> relativePaths);

    boolean isOwnerFolder(Folder folder, User user);

    List<Folder> getAllDescendantsIncludingSelf(Folder folder);
}
