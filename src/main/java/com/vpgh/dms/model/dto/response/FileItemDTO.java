package com.vpgh.dms.model.dto.response;

import com.vpgh.dms.model.dto.DocumentDTO;
import com.vpgh.dms.model.dto.FolderDTO;

public class FileItemDTO {
    private DocumentDTO document;
    private FolderDTO folder;
    private String type;
    private String permission;

    public DocumentDTO getDocument() {
        return document;
    }

    public void setDocument(DocumentDTO document) {
        this.document = document;
    }

    public FolderDTO getFolder() {
        return folder;
    }

    public void setFolder(FolderDTO folder) {
        this.folder = folder;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
}

