package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.dto.DocumentDTO;
import com.vpgh.dms.model.constant.StorageType;
import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.DocumentVersion;
import com.vpgh.dms.model.entity.Folder;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.repository.DocumentRepository;
import com.vpgh.dms.repository.DocumentVersionRepository;
import com.vpgh.dms.service.DocumentService;
import com.vpgh.dms.service.UserService;
import com.vpgh.dms.service.specification.DocumentSpecification;
import com.vpgh.dms.util.PageSize;
import com.vpgh.dms.util.exception.NotFoundException;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.*;

@Service
public class DocumentServiceImpl implements DocumentService {

    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private DocumentVersionRepository documentVersionRepository;
    @Autowired
    private S3Client s3Client;
    @Value("${aws.bucket.name}")
    private String bucketName;
    private static final String ROOT_FOLDER_PREFIX = "root";

    @Override
    public Document save(Document document) {
        return this.documentRepository.save(document);
    }

    @Override
    public Document uploadNewFile(MultipartFile file, Folder folder) throws IOException {
        return saveNewDocument(file, folder, file.getOriginalFilename());
    }

    @Override
    public Document uploadReplaceFile(MultipartFile file, Folder folder) throws IOException {
        //todo: folder null or not
        Document existingDoc = documentRepository
                .findByNameAndFolderAndIsDeletedFalse(file.getOriginalFilename(), folder)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy file để thay thế"));

        saveDocumentVersion(existingDoc);

        uploadToS3AndUpdateDoc(existingDoc, file, folder);
        return documentRepository.save(existingDoc);
    }

    @Override
    public Document uploadKeepBothFiles(MultipartFile file, Folder folder) throws IOException {
        String uniqueName = generateUniqueName(file.getOriginalFilename(), folder);
        return saveNewDocument(file, folder, uniqueName);
    }

    private Document saveNewDocument(MultipartFile file, Folder folder, String fileName) throws IOException {
        String folderPath = folder != null ? buildS3FolderPath(folder) : "";
        String storedFilename = UUID.randomUUID() + "_" + fileName;
        String key = ROOT_FOLDER_PREFIX + (folderPath.isEmpty() ? "" : "/" + folderPath) + "/" + storedFilename;

        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build(),
                RequestBody.fromBytes(file.getBytes()));

        Document doc = new Document();
        doc.setName(fileName);
        doc.setOriginalFilename(fileName);
        doc.setStoredFilename(storedFilename);
        doc.setFilePath("s3://" + bucketName + "/" + key);
        doc.setFileSize((double) file.getSize());
        doc.setMimeType(file.getContentType());
        doc.setFileHash(DigestUtils.md5DigestAsHex(file.getBytes()));
        doc.setEncrypted(false);
        doc.setStorageType(StorageType.AWS_S3);
        doc.setDeleted(false);
        doc.setFolder(folder);

        return documentRepository.save(doc);
    }

    private void saveDocumentVersion(Document document) {
        DocumentVersion version = new DocumentVersion();
        version.setName(document.getName());
        version.setVersionNumber(documentVersionRepository.countByDocument(document) + 1);
        version.setStoredFilename(document.getStoredFilename());
        version.setFilePath(document.getFilePath());
        version.setFileSize(document.getFileSize());
        version.setMimeType(document.getMimeType());
        version.setFileHash(document.getFileHash());
        version.setDocument(document);
        documentVersionRepository.save(version);
    }

    private void uploadToS3AndUpdateDoc(Document doc, MultipartFile file, Folder folder) throws IOException {
        String folderPath = folder != null ? buildS3FolderPath(folder) : "";
        String storedFilename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String key = ROOT_FOLDER_PREFIX + (folderPath.isEmpty() ? "" : "/" + folderPath) + "/" + storedFilename;

        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build(),
                RequestBody.fromBytes(file.getBytes()));

        doc.setStoredFilename(storedFilename);
        doc.setFilePath("s3://" + bucketName + "/" + key);
        doc.setFileSize((double) file.getSize());
        doc.setMimeType(file.getContentType());
        doc.setFileHash(DigestUtils.md5DigestAsHex(file.getBytes()));
    }

    private String generateUniqueName(String originalName, Folder folder) {
        String baseName = FilenameUtils.getBaseName(originalName);
        String extension = FilenameUtils.getExtension(originalName);

        String newName = originalName;
        int counter = 1;
        //todo: folder null or not
        while (documentRepository.findByNameAndFolderAndIsDeletedFalse(newName, folder).isPresent()) {
            newName = baseName + " (" + counter + ")" + (extension.isEmpty() ? "" : "." + extension);
            counter++;
        }
        return newName;
    }


    private String buildS3FolderPath(Folder folder) {
        List<String> parts = new ArrayList<>();
        while (folder != null) {
            parts.add(folder.getName());
            folder = folder.getParent();
        }
        Collections.reverse(parts);
        return String.join("/", parts);
    }

    public byte[] downloadFile(String filePath) {
        String key = extractKeyFromPath(filePath);
        ResponseBytes<GetObjectResponse> objectAsBytes = s3Client.getObjectAsBytes(GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());
        return objectAsBytes.asByteArray();
    }

    private String extractKeyFromPath(String s3Path) {
        return s3Path.replace("s3://" + bucketName + "/", "");
    }

    public void hardDelete(Document doc) {
        String key = extractKeyFromPath(doc.getFilePath());
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());
        this.documentRepository.delete(doc);
    }

    @Override
    public DocumentDTO convertDocumentToDocumentDTO(Document doc) {
        DocumentDTO dto = new DocumentDTO();
        dto.setId(doc.getId());
        dto.setName(doc.getName());
        dto.setDescription(doc.getDescription());
        dto.setOriginalFilename(doc.getOriginalFilename());
        dto.setStoredFilename(doc.getStoredFilename());
        dto.setFilePath(doc.getFilePath());
        dto.setFileSize(doc.getFileSize());
        dto.setMimeType(doc.getMimeType());
        dto.setStorageType(doc.getStorageType());
        dto.setFolder(doc.getFolder());
        dto.setCreatedAt(doc.getCreatedAt());
        dto.setUpdatedAt(doc.getUpdatedAt());
        dto.setCreatedBy(this.userService.convertUserToUserDTO(doc.getCreatedBy()));
        dto.setUpdatedBy(this.userService.convertUserToUserDTO(doc.getUpdatedBy()));

        return dto;
    }

    @Override
    public Document getDocumentByStoredFilename(String storedFileName) {
        return this.documentRepository.findByStoredFilename(storedFileName);
    }

    @Override
    public Document getDocumentById(Integer id) {
        Optional<Document> document = this.documentRepository.findById(id);
        return document.isPresent() ? document.get() : null;
    }

    @Override
    public List<Document> getDocumentsByIds(List<Integer> ids) {
        return this.documentRepository.findAllById(ids);
    }

    @Override
    public boolean existsByNameAndFolderAndIdNot(String name, Folder folder, Integer excludeId) {
        return this.documentRepository.existsByNameAndFolderAndIdNot(name, folder, excludeId);
    }

    @Override
    public boolean existsByNameAndCreatedByAndFolderIsNullAndIdNot(String name, User createdBy, Integer id) {
        return documentRepository.existsByNameAndCreatedByAndFolderIsNullAndIdNot(name, createdBy, id);
    }


    @Override
    public Page<Document> getActiveDocuments(Folder folder, User createdBy, String page) {
        Pageable pageable = PageRequest.of(Integer.parseInt(page) - 1, PageSize.DOCUMENT_PAGE_SIZE.getSize());
        return this.documentRepository.findByFolderAndCreatedByAndIsDeletedFalse(folder, createdBy, pageable);
    }

    @Override
    public Page<Document> getInactiveDocuments(Folder folder, User createdBy, String page) {
        Pageable pageable = PageRequest.of(Integer.parseInt(page) - 1, PageSize.DOCUMENT_PAGE_SIZE.getSize());
        return this.documentRepository.findByFolderAndCreatedByAndIsDeletedTrue(folder, createdBy, pageable);
    }

    @Override
    public Page<Document> searchDocuments(Map<String, String> params, User user) {
        int page = Integer.parseInt(params.get("page"));
        String kw = params.get("kw");

        Pageable pageable = PageRequest.of(page - 1, PageSize.DOCUMENT_PAGE_SIZE.getSize());
        Specification<Document> combinedSpec = Specification.allOf();
        if (kw != null && !kw.isEmpty()) {
            Specification<Document> spec = DocumentSpecification.filterByKeyword(params.get("kw"), user);
            combinedSpec = combinedSpec.and(spec);
        }

        return this.documentRepository.findAll(combinedSpec, pageable);
    }


}
