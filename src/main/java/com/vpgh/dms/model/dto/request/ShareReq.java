package com.vpgh.dms.model.dto.request;

import java.util.UUID;

import com.vpgh.dms.model.constant.ShareType;
import com.vpgh.dms.util.annotation.ValidShare;

import java.util.List;

@ValidShare
public class ShareReq {
    private UUID documentId;
    private UUID folderId;
    private List<UserShareDTO> shares;

    public static class UserShareDTO {
        private String email;
        private ShareType shareType;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public ShareType getShareType() {
            return shareType;
        }

        public void setShareType(ShareType shareType) {
            this.shareType = shareType;
        }
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public UUID getFolderId() {
        return folderId;
    }

    public void setFolderId(UUID folderId) {
        this.folderId = folderId;
    }

    public List<UserShareDTO> getShares() {
        return shares;
    }

    public void setShares(List<UserShareDTO> shares) {
        this.shares = shares;
    }
}
