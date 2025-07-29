package com.vpgh.dms.model.dto.request;

import com.vpgh.dms.util.annotation.ValidFile;
import org.springframework.web.multipart.MultipartFile;

@ValidFile
public class FileUploadReq {
    MultipartFile file;

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}
