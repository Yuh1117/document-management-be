package com.vpgh.dms.model.dto.request;

import com.vpgh.dms.util.annotation.ValidFile;
import org.springframework.web.multipart.MultipartFile;

@ValidFile
public class FileUploadReq {
    private MultipartFile file;
    private Integer folderId;

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public Integer getFolderId() {
        return folderId;
    }

    public void setFolderId(Integer folderId) {
        this.folderId = folderId;
    }
}

