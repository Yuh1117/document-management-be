package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.dto.FolderDTO;
import com.vpgh.dms.model.dto.SubFolderDTO;
import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.Folder;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.repository.DocumentRepository;
import com.vpgh.dms.repository.FolderRepository;
import com.vpgh.dms.service.DocumentService;
import com.vpgh.dms.service.FolderService;
import com.vpgh.dms.service.UserService;
import com.vpgh.dms.util.PathUtil;
import com.vpgh.dms.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FolderServiceImpl implements FolderService {

    @Autowired
    private FolderRepository folderRepository;
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private DocumentService documentService;
    @Autowired
    private UserService userService;
    @Autowired
    private S3Client s3Client;
    @Value("${aws.bucket.name}")
    private String bucketName;

    @Override
    public Folder getFolderById(Integer id) {
        Optional<Folder> folder = this.folderRepository.findById(id);
        return folder.orElse(null);
    }

    @Override
    public List<Folder> getFoldersByIds(List<Integer> ids) {
        return this.folderRepository.findByIdIn(ids);
    }

    @Override
    public boolean existsByNameAndParentAndIsDeletedFalseAndIdNot(String name, Folder parent, Integer id) {
        return this.folderRepository.existsByNameAndParentAndIsDeletedFalseAndIdNot(name, parent, id);
    }

    @Override
    public boolean existsByNameAndCreatedByAndParentIsNullAndIsDeletedFalseAndIdNot(String name, User createdBy, Integer id) {
        return this.folderRepository.existsByNameAndCreatedByAndParentIsNullAndIsDeletedFalseAndIdNot(name, createdBy, id);
    }


    @Override
    public Folder save(Folder folder) {
        return this.folderRepository.save(folder);
    }

    @Override
    public void softDeleteFolderAndChildren(Folder folder) {
        Stack<Folder> stack = new Stack<>();
        stack.push(folder);

        while (!stack.isEmpty()) {
            Folder currentFolder = stack.pop();
            currentFolder.setDeleted(true);
            this.folderRepository.save(currentFolder);

            List<Document> documents = this.documentRepository.findByFolderAndIsDeletedFalse(currentFolder);
            for (Document doc : documents) {
                doc.setDeleted(true);
                this.documentRepository.save(doc);
            }

            List<Folder> subFolders = this.folderRepository.findByParentAndIsDeletedFalse(currentFolder);
            for (Folder subFolder : subFolders) {
                stack.push(subFolder);
            }
        }
    }

    @Override
    public void restoreFolderAndChildren(Folder folder) {
        Stack<Folder> stack = new Stack<>();
        stack.push(folder);

        while (!stack.isEmpty()) {
            Folder currentFolder = stack.pop();
            currentFolder.setDeleted(false);
            this.folderRepository.save(currentFolder);

            List<Document> documents = this.documentRepository.findByFolderAndIsDeletedTrue(currentFolder);
            for (Document doc : documents) {
                doc.setDeleted(false);
                this.documentRepository.save(doc);
            }

            List<Folder> subFolders = this.folderRepository.findByParentAndIsDeletedTrue(currentFolder);
            for (Folder subFolder : subFolders) {
                stack.push(subFolder);
            }
        }
    }

    @Override
    public void hardDeleteFolderAndChildren(Folder folder) {
        Stack<Folder> stack = new Stack<>();
        stack.push(folder);

        while (!stack.isEmpty()) {
            Folder currentFolder = stack.pop();

            List<Document> documents = this.documentRepository.findByFolderId(currentFolder.getId());
            for (Document doc : documents) {
                if (Boolean.TRUE.equals(doc.getDeleted())) {
                    s3Client.deleteObject(DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(extractKeyFromPath(doc.getFilePath()))
                            .build());
                    this.documentRepository.delete(doc);
                }
            }

            List<Folder> subFolders = this.folderRepository.findByParentId(currentFolder.getId());
            for (Folder subFolder : subFolders) {
                stack.push(subFolder);
            }

            this.folderRepository.delete(currentFolder);
        }
    }

    private String extractKeyFromPath(String s3Path) {
        return s3Path.replace("s3://" + bucketName + "/", "");
    }

    @Override
    public List<Folder> findByParentAndIsDeletedFalse(Folder parent) {
        return this.folderRepository.findByParentAndIsDeletedFalse(parent);
    }

    @Override
    public List<Folder> findByParentAndIsDeletedTrue(Folder parent) {
        return this.folderRepository.findByParentAndIsDeletedTrue(parent);
    }

    @Override
    public void copyFolder(Folder folder, Folder targetFolder) {
        Stack<Folder> stack = new Stack<>();
        stack.push(folder);
        Map<Integer, Folder> copiedMap = new HashMap<>();

        Folder rootCopy = new Folder();
        rootCopy.setName(generateUniqueName(folder.getName(), targetFolder));
        rootCopy.setParent(targetFolder);
        this.folderRepository.save(rootCopy);
        copiedMap.put(folder.getId(), rootCopy);

        while (!stack.isEmpty()) {
            Folder current = stack.pop();
            Folder currentCopy = copiedMap.get(current.getId());

            List<Document> docs = this.documentRepository.findByFolderId(current.getId());
            for (Document doc : docs) {
                this.documentService.copyDocument(doc, currentCopy);
            }

            List<Folder> subFolders = this.folderRepository.findByParentId(current.getId());
            for (Folder subFolder : subFolders) {
                Folder subCopy = new Folder();
                subCopy.setName(generateUniqueName(subFolder.getName(), currentCopy));
                subCopy.setParent(currentCopy);
                this.folderRepository.save(subCopy);

                copiedMap.put(subFolder.getId(), subCopy);
                stack.push(subFolder);
            }
        }
    }

    @Override
    public void moveFolder(Folder folder, Folder targetFolder) {
        Stack<Folder> stack = new Stack<>();
        stack.push(folder);
        Map<Integer, Folder> movedMap = new HashMap<>();

        while (!stack.isEmpty()) {
            Folder currentFolder = stack.pop();
            List<Folder> subFolders = folderRepository.findByParentId(currentFolder.getId());

            Folder newParent;
            if (currentFolder.getId().equals(folder.getId())) {
                newParent = targetFolder;
            } else {
                newParent = movedMap.get(currentFolder.getParent().getId());
            }

            String newName = generateUniqueName(currentFolder.getName(), newParent);
            currentFolder.setName(newName);
            currentFolder.setParent(newParent);
            folderRepository.save(currentFolder);

            List<Document> docs = documentRepository.findByFolderId(currentFolder.getId());
            for (Document doc : docs) {
                documentService.moveDocument(doc, currentFolder);
            }

            movedMap.put(currentFolder.getId(), currentFolder);

            stack.addAll(subFolders);
        }
    }

    @Override
    public FolderDTO convertFolderToFolderDTO(Folder folder) {
        FolderDTO dto = new FolderDTO();
        dto.setId(folder.getId());
        dto.setName(folder.getName());
        dto.setDeleted(folder.getDeleted());
        dto.setInheritPermissions(folder.getInheritPermissions());
        dto.setDocuments(new HashSet<>(this.documentService.convertDocumentsToDocumentDTOs(new ArrayList<>(folder.getDocuments()))));
        dto.setFolders(new HashSet<>(convertFoldersToSubFolderDTOs(new ArrayList<>(folder.getFolders()))));
        dto.setCreatedAt(folder.getCreatedAt());
        dto.setUpdatedAt(folder.getUpdatedAt());
        dto.setCreatedBy(this.userService.convertUserToUserDTO(folder.getCreatedBy()));
        dto.setUpdatedBy(folder.getUpdatedBy() != null ? this.userService.convertUserToUserDTO(folder.getUpdatedBy()) : null);

        return dto;
    }

    @Override
    public List<FolderDTO> convertFoldersToFolderDTOs(List<Folder> folders) {
        return folders.stream().map(f -> convertFolderToFolderDTO(f)).collect(Collectors.toList());
    }

    @Override
    public SubFolderDTO convertFolderToSubFolderDTO(Folder folder) {
        SubFolderDTO dto = new SubFolderDTO();
        dto.setId(folder.getId());
        dto.setName(folder.getName());
        dto.setInheritPermissions(folder.getInheritPermissions());
        dto.setDeleted(folder.getDeleted());
        dto.setCreatedAt(folder.getCreatedAt());
        dto.setUpdatedAt(folder.getUpdatedAt());
        dto.setCreatedBy(this.userService.convertUserToUserDTO(folder.getCreatedBy()));
        dto.setUpdatedBy(folder.getUpdatedBy() != null ? this.userService.convertUserToUserDTO(folder.getUpdatedBy()) : null);
        return dto;
    }

    @Override
    public List<SubFolderDTO> convertFoldersToSubFolderDTOs(List<Folder> folders) {
        return folders.stream().map(f -> convertFolderToSubFolderDTO(f)).collect(Collectors.toList());
    }

    @Override
    public void zipFolderIterative(Folder rootFolder, ZipOutputStream zipOut) throws IOException {
        Stack<Folder> stack = new Stack<>();
        stack.push(rootFolder);

        while (!stack.isEmpty()) {
            Folder folder = stack.pop();

            List<Folder> subFolders = this.folderRepository.findByParentId(folder.getId());
            List<Document> docs = this.documentRepository.findByFolderId(folder.getId());

            if (docs.isEmpty() && subFolders.isEmpty()) {
                String relativePath = PathUtil.buildRelativePath(folder, rootFolder) + "/";
                zipOut.putNextEntry(new ZipEntry(relativePath));
                zipOut.closeEntry();
            }

            for (Document doc : docs) {
                try (InputStream in = this.documentService.downloadFileStream(doc.getFilePath())) {
                    String relativePath = PathUtil.buildRelativePath(doc, rootFolder);
                    zipOut.putNextEntry(new ZipEntry(relativePath));
                    in.transferTo(zipOut);
                    zipOut.closeEntry();
                }
            }

            for (Folder sub : subFolders) {
                stack.push(sub);
            }
        }
    }

    @Override
    public Folder uploadNewFolder(Folder parentFolder, List<MultipartFile> files, List<String> relativePaths) throws IOException {
        Folder rootFolder = null;

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String relativePath = relativePaths.get(i);

            Folder currentParent = parentFolder;

            if (relativePath != null && !relativePath.isEmpty()) {
                String[] parts = relativePath.split("/");

                for (int j = 0; j < parts.length - 1; j++) {
                    String folderName = parts[j];
                    Folder existing = folderRepository.findByNameAndParentAndIsDeletedFalse(folderName, currentParent);
                    if (existing == null) {
                        Folder newFolder = new Folder();
                        newFolder.setName(folderName);
                        newFolder.setParent(currentParent);
                        currentParent = folderRepository.save(newFolder);
                    } else {
                        currentParent = existing;
                    }
                    if (j == 0 && rootFolder == null) {
                        rootFolder = currentParent;
                    }
                }
            }

            this.documentService.uploadNewFile(file, currentParent);
        }

        return rootFolder;
    }

    private String generateUniqueName(String originalName, Folder targetFolder) {
        String newName = originalName;
        int counter = 1;
        if (targetFolder != null) {
            while (this.folderRepository.existsByNameAndParentAndIsDeletedFalseAndIdNot(newName, targetFolder, null)) {
                newName = originalName + " (" + counter++ + ")";
            }
        } else {
            while (this.folderRepository.existsByNameAndCreatedByAndParentIsNullAndIsDeletedFalseAndIdNot(newName,
                    SecurityUtil.getCurrentUserFromThreadLocal(), null)) {
                newName = originalName + " (" + counter++ + ")";
            }
        }
        return newName;
    }
}
