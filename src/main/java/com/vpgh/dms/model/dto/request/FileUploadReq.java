package com.vpgh.dms.model.dto.request;

import com.vpgh.dms.util.annotation.ValidFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@ValidFile
public class FileUploadReq {
    private List<MultipartFile> files;
    private Integer folderId;

    public List<MultipartFile> getFiles() {
        return files;
    }

    public void setFiles(List<MultipartFile> files) {
        this.files = files;
    }

    public Integer getFolderId() {
        return folderId;
    }

    public void setFolderId(Integer folderId) {
        this.folderId = folderId;
    }
}