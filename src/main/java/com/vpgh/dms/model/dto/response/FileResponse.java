package com.vpgh.dms.model.dto.response;

import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.Folder;

import java.util.List;

public class FileResponse {
    private List<Folder> folders;
    private List<Document> documents;

    public FileResponse(List<Folder> folders, List<Document> documents) {
        this.folders = folders;
        this.documents = documents;
    }

    public List<Folder> getFolders() {
        return folders;
    }

    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }
}

