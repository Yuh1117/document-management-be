package com.vpgh.dms.model.dto.request;

import com.vpgh.dms.util.annotation.ValidFile;
import org.springframework.web.multipart.MultipartFile;

@ValidFile
public class FileUploadReq {
    private MultipartFile[] files;
    private Integer folderId;

    public MultipartFile[] getFiles() {
        return files;
    }

    public void setFiles(MultipartFile[] files) {
        this.files = files;
    }

    public Integer getFolderId() {
        return folderId;
    }

    public void setFolderId(Integer folderId) {
        this.folderId = folderId;
    }
}