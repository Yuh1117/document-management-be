package com.vpgh.dms.model.dto.request;

import java.util.List;

public class CopyCutReq {
    private List<Integer> ids;
    private Integer targetFolderId;

    public List<Integer> getIds() {
        return ids;
    }

    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }

    public Integer getTargetFolderId() {
        return targetFolderId;
    }

    public void setTargetFolderId(Integer targetFolderId) {
        this.targetFolderId = targetFolderId;
    }
}
