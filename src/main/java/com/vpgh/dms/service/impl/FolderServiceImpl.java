package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.Folder;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.repository.DocumentRepository;
import com.vpgh.dms.repository.FolderRepository;
import com.vpgh.dms.service.FolderService;
import com.vpgh.dms.service.specification.FolderSpecification;
import com.vpgh.dms.util.PageSize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

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
        Stack<Folder> stack = new Stack<>();
        stack.push(folder);

        while (!stack.isEmpty()) {
            Folder currentFolder = stack.pop();
            currentFolder.setDeleted(true);
            this.folderRepository.save(currentFolder);

            List<Document> documents = this.documentRepository.findByFolderAndCreatedByAndIsDeletedFalse(currentFolder,
                    currentFolder.getCreatedBy(), Pageable.unpaged()).getContent();
            for (Document doc : documents) {
                doc.setDeleted(true);
                this.documentRepository.save(doc);
            }

            List<Folder> subFolders = this.folderRepository.findByParentAndCreatedByAndIsDeletedFalse(currentFolder,
                    currentFolder.getCreatedBy(), Pageable.unpaged()).getContent();
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

            List<Document> documents = this.documentRepository.findByFolderAndCreatedByAndIsDeletedTrue(currentFolder,
                    currentFolder.getCreatedBy(), Pageable.unpaged()).getContent();
            for (Document doc : documents) {
                doc.setDeleted(false);
                this.documentRepository.save(doc);
            }

            List<Folder> subFolders = this.folderRepository.findByParentAndCreatedByAndIsDeletedTrue(currentFolder,
                    currentFolder.getCreatedBy(), Pageable.unpaged()).getContent();
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
    public Page<Folder> getActiveFolders(Folder parent, User createdBy, String page) {
        Pageable pageable = PageRequest.of(Integer.parseInt(page) - 1, PageSize.FOLDER_PAGE_SIZE.getSize());
        return this.folderRepository.findByParentAndCreatedByAndIsDeletedFalse(parent, createdBy, pageable);
    }

    @Override
    public Page<Folder> getInactiveFolders(Folder parent, User createdBy, String page) {
        Pageable pageable = PageRequest.of(Integer.parseInt(page) - 1, PageSize.FOLDER_PAGE_SIZE.getSize());
        return this.folderRepository.findByParentAndCreatedByAndIsDeletedTrue(parent, createdBy, pageable);
    }

    @Override
    public Page<Folder> searchFolders(Map<String, String> params, User user) {
        int page = Integer.parseInt(params.get("page"));
        String kw = params.get("kw");

        Pageable pageable = PageRequest.of(page - 1, PageSize.FOLDER_PAGE_SIZE.getSize());
        Specification<Folder> combinedSpec = Specification.allOf();
        if (kw != null && !kw.isEmpty()) {
            Specification<Folder> spec = FolderSpecification.filterByKeyword(params.get("kw"), user);
            combinedSpec = combinedSpec.and(spec);
        }

        return this.folderRepository.findAll(combinedSpec, pageable);
    }
}
