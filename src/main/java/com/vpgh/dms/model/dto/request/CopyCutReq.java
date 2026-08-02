package com.vpgh.dms.model.dto.request;

import java.util.UUID;

import java.util.List;

public class CopyCutReq {
    private List<UUID> ids;
    private UUID targetFolderId;

    public List<UUID> getIds() {
        return ids;
    }

    public void setIds(List<UUID> ids) {
        this.ids = ids;
    }

    public UUID getTargetFolderId() {
        return targetFolderId;
    }

    public void setTargetFolderId(UUID targetFolderId) {
        this.targetFolderId = targetFolderId;
    }
}
