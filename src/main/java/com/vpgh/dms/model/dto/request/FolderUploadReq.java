package com.vpgh.dms.model.dto.request;

import java.util.UUID;

import com.vpgh.dms.util.annotation.ValidFolder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@ValidFolder
public class FolderUploadReq {
    private UUID parentId;
    private List<MultipartFile> files;
    private List<String> relativePaths;

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

    public List<MultipartFile> getFiles() {
        return files;
    }

    public void setFiles(List<MultipartFile> files) {
        this.files = files;
    }

    public List<String> getRelativePaths() {
        return relativePaths;
    }

    public void setRelativePaths(List<String> relativePaths) {
        this.relativePaths = relativePaths;
    }
}
