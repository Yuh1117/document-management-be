package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.dto.DocumentDTO;
import com.vpgh.dms.model.constant.StorageType;
import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.DocumentVersion;
import com.vpgh.dms.model.entity.Folder;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.repository.DocumentRepository;
import com.vpgh.dms.repository.DocumentVersionRepository;
import com.vpgh.dms.service.DocumentParseService;
import com.vpgh.dms.service.DocumentService;
import com.vpgh.dms.service.UserService;
import com.vpgh.dms.util.SecurityUtil;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final UserService userService;
    private final DocumentVersionRepository documentVersionRepository;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final DocumentParseService documentParseService;
    @Value("${aws.bucket.name}")
    private String bucketName;
    private static final String ROOT_FOLDER_PREFIX = "root";

    public DocumentServiceImpl(S3Presigner s3Presigner, S3Client s3Client, DocumentVersionRepository documentVersionRepository,
                               UserService userService, DocumentRepository documentRepository,
                               DocumentParseService documentParseService) {
        this.s3Presigner = s3Presigner;
        this.s3Client = s3Client;
        this.documentVersionRepository = documentVersionRepository;
        this.userService = userService;
        this.documentRepository = documentRepository;
        this.documentParseService = documentParseService;
    }

    @Override
    public Document save(Document document) {
        return this.documentRepository.save(document);
    }

    @Override
    public Document uploadNewFile(MultipartFile file, Folder folder) throws IOException {
        String fileName = Paths.get(file.getOriginalFilename()).getFileName().toString();
        return saveNewDocument(file, folder, fileName);
    }

    @Override
    public Document uploadReplaceFile(MultipartFile file, Folder folder, Document existingDoc) throws IOException {
        saveDocumentVersion(existingDoc);

        String folderPath = buildS3FolderPath(folder);
        String storedFilename = generateStoredFilename(file.getOriginalFilename());
        String key = buildS3Key(folderPath, storedFilename);

        File tempFile = convertMultipartToFile(file);
        try (InputStream inputStream = new FileInputStream(tempFile)) {
            uploadToS3(key, inputStream, tempFile.length());
        }

        existingDoc.setStoredFilename(storedFilename);
        existingDoc.setFilePath(buildS3Uri(key));
        existingDoc.setFileSize((double) tempFile.length());
        existingDoc.setMimeType(file.getContentType());
        byte[] fileBytes = Files.readAllBytes(tempFile.toPath());
        existingDoc.setFileHash(DigestUtils.md5DigestAsHex(fileBytes));

        Document saved = this.documentRepository.save(existingDoc);
        this.documentParseService.parseAndIndexAsync(saved, tempFile);
        return saved;
    }

    @Override
    public Document uploadKeepBothFiles(MultipartFile file, Folder folder) throws IOException {
        String uniqueName = generateUniqueName(file.getOriginalFilename(), folder);
        return saveNewDocument(file, folder, uniqueName);
    }


//    @Override
//    public byte[] downloadFile(String filePath) {
//        String key = extractKeyFromPath(filePath);
//        ResponseBytes<GetObjectResponse> objectAsBytes = s3Client.getObjectAsBytes(GetObjectRequest.builder()
//                .bucket(bucketName)
//                .key(key)
//                .build());
//        return objectAsBytes.asByteArray();
//    }

    @Override
    public InputStream downloadFileStream(String filePath) {
        String key = extractKeyFromPath(filePath);
        return s3Client.getObject(GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());
    }


    @Override
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
        dto.setDeleted(doc.getDeleted());
        dto.setCreatedAt(doc.getCreatedAt());
        dto.setUpdatedAt(doc.getUpdatedAt());
        dto.setCreatedBy(this.userService.convertUserToUserDTO(doc.getCreatedBy()));
        dto.setUpdatedBy(doc.getUpdatedBy() != null ? this.userService.convertUserToUserDTO(doc.getUpdatedBy()) : null);

        return dto;
    }

    @Override
    public List<DocumentDTO> convertDocumentsToDocumentDTOs(List<Document> docs) {
        return docs.stream().map(d -> convertDocumentToDocumentDTO(d)).collect(Collectors.toList());
    }

    @Override
    public Document getDocumentByStoredFilename(String storedFileName) {
        return this.documentRepository.findByStoredFilename(storedFileName);
    }

    @Override
    public Document getDocumentById(Integer id) {
        Optional<Document> document = this.documentRepository.findById(id);
        return document.orElse(null);
    }

    @Override
    public List<Document> getDocumentsByIds(List<Integer> ids) {
        return this.documentRepository.findByIdIn(ids);
    }

    @Override
    public List<Document> getAllDocumentsInFolders(List<Folder> folders) {
        return this.documentRepository.findByFolderIn(folders);
    }

    @Override
    public boolean existsByNameAndFolderAndIsDeletedFalseAndIdNot(String name, Folder folder, Integer excludeId) {
        return this.documentRepository.existsByNameAndFolderAndIsDeletedFalseAndIdNot(name, folder, excludeId);
    }

    @Override
    public boolean existsByNameAndCreatedByAndFolderIsNullAndIsDeletedFalseAndIdNot(String name, User createdBy, Integer id) {
        return this.documentRepository.existsByNameAndCreatedByAndFolderIsNullAndIsDeletedFalseAndIdNot(name, createdBy, id);
    }

    @Override
    public Document findByNameAndFolderAndIsDeletedFalse(String name, Folder folder) {
        return this.documentRepository.findByNameAndFolderAndIsDeletedFalse(name, folder).orElse(null);
    }

    @Override
    public Document findByNameAndCreatedByAndFolderIsNullAndIsDeletedFalse(String name, User createdBy) {
        return this.documentRepository.findByNameAndCreatedByAndFolderIsNullAndIsDeletedFalse(name, createdBy).orElse(null);
    }

    @Override
    public List<Document> findByFolderAndIsDeletedFalse(Folder folder) {
        return this.documentRepository.findByFolderAndIsDeletedFalse(folder);
    }

    @Override
    public List<Document> findByFolderAndIsDeletedTrue(Folder folder) {
        return this.documentRepository.findByFolderAndIsDeletedTrue(folder);
    }

    @Override
    public void copyDocument(Document doc, Folder targetFolder) {
        String uniqueName = generateUniqueName(doc.getName(), targetFolder);
        String sourceKey = extractKeyFromPath(doc.getFilePath());
        String folderPath = buildS3FolderPath(targetFolder);
        String copiedFilename = generateStoredFilename(doc.getOriginalFilename());
        String targetKey = buildS3Key(folderPath, copiedFilename);

        s3Client.copyObject(CopyObjectRequest.builder()
                .sourceBucket(bucketName)
                .sourceKey(sourceKey)
                .destinationBucket(bucketName)
                .destinationKey(targetKey)
                .build());

        Document copy = new Document();
        copy.setName(uniqueName);
        copy.setOriginalFilename(doc.getOriginalFilename());
        copy.setStoredFilename(copiedFilename);
        copy.setFilePath(buildS3Uri(targetKey));
        copy.setFileSize(doc.getFileSize());
        copy.setMimeType(doc.getMimeType());
        copy.setFileHash(doc.getFileHash());
        copy.setEncrypted(doc.getEncrypted());
        copy.setStorageType(doc.getStorageType());
        copy.setFolder(targetFolder);

        this.documentRepository.save(copy);
    }

    @Override
    public void moveDocument(Document doc, Folder targetFolder) {
        String oldKey = extractKeyFromPath(doc.getFilePath());
        String folderPath = buildS3FolderPath(targetFolder);
        String storedFilename = doc.getStoredFilename();
        String newKey = buildS3Key(folderPath, storedFilename);

        s3Client.copyObject(CopyObjectRequest.builder()
                .sourceBucket(bucketName)
                .sourceKey(oldKey)
                .destinationBucket(bucketName)
                .destinationKey(newKey)
                .build());

        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(oldKey)
                .build());

        String newName = doc.getName();
        if (!Objects.equals(doc.getFolder(), targetFolder)) {
            newName = generateUniqueName(doc.getName(), targetFolder);
        }
        doc.setFilePath(buildS3Uri(newKey));
        doc.setFolder(targetFolder);
        doc.setName(newName);

        this.documentRepository.save(doc);
    }

    @Override
    public String generateSignedUrl(Document doc, int expiryInMinutes) {
        String key = extractKeyFromPath(doc.getFilePath());

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expiryInMinutes))
                .getObjectRequest(GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .responseCacheControl("no-store")
                        .responseContentDisposition("inline; filename=\"" + doc.getName() + "\"")
                        .responseContentType(doc.getMimeType())
                        .build())
                .build();

        URL signedUrl = s3Presigner.presignGetObject(presignRequest).url();

        return signedUrl.toString();
    }

    @Override
    public boolean isOwnerDocument(Document doc, User user) {
        return doc.getCreatedBy().getId().equals(user.getId());
    }

    private Document saveNewDocument(MultipartFile file, Folder folder, String fileName) throws IOException {
        String folderPath = buildS3FolderPath(folder);
        String storedFilename = generateStoredFilename(fileName);
        String key = buildS3Key(folderPath, storedFilename);

        File tempFile = convertMultipartToFile(file);
        try (InputStream inputStream = new FileInputStream(tempFile)) {
            uploadToS3(key, inputStream, tempFile.length());
        }

        Document doc = new Document();
        doc.setName(fileName);
        doc.setOriginalFilename(fileName);
        doc.setStoredFilename(storedFilename);
        doc.setFilePath(buildS3Uri(key));
        doc.setFileSize((double) tempFile.length());
        doc.setMimeType(file.getContentType());
        byte[] fileBytes = Files.readAllBytes(tempFile.toPath());
        doc.setFileHash(DigestUtils.md5DigestAsHex(fileBytes));
        doc.setStorageType(StorageType.AWS_S3);
        doc.setFolder(folder);

        Document saved = documentRepository.save(doc);
        this.documentParseService.parseAndIndexAsync(saved, tempFile);

        return saved;
    }

    private File convertMultipartToFile(MultipartFile file) throws IOException {
        File convFile = File.createTempFile("upload-", file.getOriginalFilename());
        file.transferTo(convFile);
        return convFile;
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
        this.documentVersionRepository.save(version);
    }

    private void uploadToS3(String key, InputStream inputStream, long size) throws IOException {
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build(),
                RequestBody.fromInputStream(inputStream, size));
    }

    private String generateUniqueName(String originalName, Folder folder) {
        String baseName = FilenameUtils.getBaseName(originalName);
        String extension = FilenameUtils.getExtension(originalName);
        baseName = baseName.replaceAll("\\s*\\(\\d+\\)$", "");

        String newName = originalName;
        int counter = 1;
        if (folder != null) {
            while (this.documentRepository.findByNameAndFolderAndIsDeletedFalse(newName, folder).isPresent()) {
                newName = baseName + " (" + counter++ + ")" + (extension.isEmpty() ? "" : "." + extension);
            }
        } else {
            while (this.documentRepository.findByNameAndCreatedByAndFolderIsNullAndIsDeletedFalse(
                    newName, SecurityUtil.getCurrentUserFromThreadLocal()).isPresent()) {
                newName = baseName + " (" + counter++ + ")" + (extension.isEmpty() ? "" : "." + extension);
            }
        }

        return newName;
    }

    private String buildS3Uri(String key) {
        return "s3://" + bucketName + "/" + key;
    }

    private String buildS3Key(String folderPath, String storedFilename) {
        return ROOT_FOLDER_PREFIX + "/" + folderPath + "/" + storedFilename;
    }

    private String generateStoredFilename(String originalFilename) {
        return UUID.randomUUID() + "_" + originalFilename;
    }

    private String buildS3FolderPath(Folder folder) {
        List<String> parts = new ArrayList<>();
        parts.add(folder != null ? folder.getCreatedBy().getEmail() : SecurityUtil.getCurrentUserFromThreadLocal().getEmail());

        while (folder != null) {
            parts.add(folder.getName());
            folder = folder.getParent();
        }
        Collections.reverse(parts.subList(1, parts.size()));
        return String.join("/", parts);
    }

    private String extractKeyFromPath(String s3Path) {
        return s3Path.replace("s3://" + bucketName + "/", "");
    }
}
