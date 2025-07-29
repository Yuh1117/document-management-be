package com.vpgh.dms.service;

import com.vpgh.dms.model.DocumentDTO;
import com.vpgh.dms.model.entity.Document;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface DocumentService {
    Document uploadFile(MultipartFile file) throws IOException;

    byte[] downloadFile(String key);

    Document save(Document document);

    DocumentDTO convertDocumentToDocumentDTO(Document doc);
}
