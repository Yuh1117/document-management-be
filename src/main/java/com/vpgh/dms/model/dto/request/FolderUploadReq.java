package com.vpgh.dms.model.dto.request;

import com.vpgh.dms.util.annotation.ValidFolder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@ValidFolder
public class FolderUploadReq {
    private Integer parentId;
    private List<MultipartFile> files;
    private List<String> relativePaths;

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
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
