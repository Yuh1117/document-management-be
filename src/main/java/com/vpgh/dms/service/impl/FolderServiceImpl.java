package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.Folder;
import com.vpgh.dms.repository.DocumentRepository;
import com.vpgh.dms.repository.FolderRepository;
import com.vpgh.dms.service.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.util.List;
import java.util.Optional;

@Service
public class FolderServiceImpl implements FolderService {

    @Autowired
    private FolderRepository folderRepository;
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private S3Client s3Client;
    @Value("${aws.bucket.name}")
    private String bucketName;

    @Override
    public Folder getFolderById(Integer id) {
        Optional<Folder> folder = this.folderRepository.findById(id);
        return folder.isPresent() ? folder.get() : null;
    }

    @Override
    public boolean existsByNameAndParentAndIdNot(String name, Folder parent, Integer excludeId) {
        return this.folderRepository.existsByNameAndParentAndIdNot(name, parent, excludeId);
    }

    @Override
    public Folder save(Folder folder) {
        return this.folderRepository.save(folder);
    }

    @Override
    public void softDeleteFolderAndChildren(Folder folder) {
        folder.setDeleted(true);
        this.folderRepository.save(folder);

        List<Document> documents = this.documentRepository.findByFolderIdAndIsDeletedFalse(folder.getId());
        for (Document doc : documents) {
            doc.setDeleted(true);
            this.documentRepository.save(doc);
        }

        List<Folder> subFolders = this.folderRepository.findByParentIdAndIsDeletedFalse(folder.getId());
        for (Folder subFolder : subFolders) {
            softDeleteFolderAndChildren(subFolder);
        }
    }

    @Override
    public void restoreFolderAndChildren(Folder folder) {
        folder.setDeleted(false);
        this.folderRepository.save(folder);

        List<Document> documents = this.documentRepository.findByFolderIdAndIsDeletedTrue(folder.getId());
        for (Document doc : documents) {
            doc.setDeleted(false);
            this.documentRepository.save(doc);
        }

        List<Folder> subFolders = this.folderRepository.findByParentIdAndIsDeletedTrue(folder.getId());
        for (Folder subFolder : subFolders) {
            restoreFolderAndChildren(subFolder);
        }
    }

    @Override
    public void hardDeleteFolderAndChildren(Folder folder) {
        List<Document> documents = this.documentRepository.findByFolderId(folder.getId());
        for (Document doc : documents) {
            if (Boolean.TRUE.equals(doc.getDeleted())) {
                s3Client.deleteObject(DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(extractKeyFromPath(doc.getFilePath()))
                        .build());
                this.documentRepository.delete(doc);
            }
        }

        List<Folder> subFolders = this.folderRepository.findByParentId(folder.getId());
        for (Folder subFolder : subFolders) {
            hardDeleteFolderAndChildren(subFolder);
        }

        this.folderRepository.delete(folder);
    }

    private String extractKeyFromPath(String s3Path) {
        return s3Path.replace("s3://" + bucketName + "/", "");
    }

}
