package com.vpgh.dms.model.dto.response;

import com.vpgh.dms.model.dto.DocumentDTO;
import com.vpgh.dms.model.dto.FolderDTO;

import java.util.List;

public class FileResponse {
    private List<FolderDTO> folders;
    private List<DocumentDTO> documents;

    public FileResponse(List<FolderDTO> folders, List<DocumentDTO> documents) {
        this.folders = folders;
        this.documents = documents;
    }

    public List<FolderDTO> getFolders() {
        return folders;
    }

    public void setFolders(List<FolderDTO> folders) {
        this.folders = folders;
    }

    public List<DocumentDTO> getDocuments() {
        return documents;
    }

    public void setDocuments(List<DocumentDTO> documents) {
        this.documents = documents;
    }
}

