package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.constant.StorageType;
import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.repository.DocumentRepository;
import com.vpgh.dms.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class DocumentServiceImpl implements DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private S3Client s3Client;

    @Value("${aws.bucket.name}")
    private String bucketName;

    @Override
    public Document save(Document document) {
        return this.documentRepository.save(document);
    }

    public Document uploadFile(MultipartFile file) throws IOException {
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(file.getOriginalFilename())
                        .build(),
                RequestBody.fromBytes(file.getBytes()));

        String storedFilename = UUID.randomUUID() + "_" + file.getOriginalFilename();

        Document doc = new Document();
        doc.setName(file.getOriginalFilename());
        doc.setOriginalFilename(file.getOriginalFilename());
        doc.setStoredFilename(storedFilename);
        doc.setFilePath("s3://" + bucketName + "/" + file.getOriginalFilename());
        doc.setFileSize((double) file.getSize());
        doc.setMimeType(file.getContentType());
        doc.setFileHash(DigestUtils.md5DigestAsHex(file.getBytes()));
        doc.setEncrypted(false);
        doc.setStorageType(StorageType.AWS_S3);
        doc.setDeleted(false);

        return this.documentRepository.save(doc);
    }

    public byte[] downloadFile(String key) {
        ResponseBytes<GetObjectResponse> objectAsBytes = s3Client.getObjectAsBytes(GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());
        return objectAsBytes.asByteArray();
    }

}
