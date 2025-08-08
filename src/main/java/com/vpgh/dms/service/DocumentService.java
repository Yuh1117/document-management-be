package com.vpgh.dms.service;

import com.vpgh.dms.model.dto.DocumentDTO;
import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.Folder;
import com.vpgh.dms.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface DocumentService {
    Document uploadFile(MultipartFile file, Folder folder) throws IOException;

    byte[] downloadFile(String key);

    Document save(Document document);

    DocumentDTO convertDocumentToDocumentDTO(Document doc);

    Document getDocumentByStoredFilename(String storedFileName);

    Document getDocumentById(Integer id);

    void hardDelete(Document doc);

    List<Document> getDocumentsByIds(List<Integer> ids);

    boolean existsByNameAndFolderAndCreatedByAndIdNot(String name, Folder folder, User user, Integer excludeId);

    Page<Document> getActiveDocuments(Folder folder, User createdBy, String page);

    Page<Document> getInactiveDocuments(Folder folder, User createdBy, String page);

    Page<Document> searchDocuments(Map<String, String> params, User user);
}
