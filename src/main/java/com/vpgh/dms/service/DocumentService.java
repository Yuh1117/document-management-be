package com.vpgh.dms.service;

import com.vpgh.dms.model.dto.DocumentDTO;
import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.Folder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface DocumentService {
    Document uploadFile(MultipartFile file, Folder folder) throws IOException;

    byte[] downloadFile(String key);

    Document save(Document document);

    DocumentDTO convertDocumentToDocumentDTO(Document doc);

    Document getDocumentByStoredFilename(String storedFileName);

    Document getDocumentById(Integer id);

    void hardDelete(Document doc);

    List<Document> getDocumentsByIds(List<Integer> ids);

    boolean existsByNameAndFolderAndIdNot(String name, Folder folder, Integer excludeId);
}
