package com.vpgh.dms.model.dto.request;

import java.util.UUID;

import com.vpgh.dms.util.annotation.ValidFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@ValidFile
public class FileUploadReq {
    private List<MultipartFile> files;
    private UUID folderId;

    public List<MultipartFile> getFiles() {
        return files;
    }

    public void setFiles(List<MultipartFile> files) {
        this.files = files;
    }

    public UUID getFolderId() {
        return folderId;
    }

    public void setFolderId(UUID folderId) {
        this.folderId = folderId;
    }
}
