package com.vpgh.dms.model.dto.request;

import com.vpgh.dms.model.constant.ShareType;
import com.vpgh.dms.util.annotation.ValidShare;

import java.util.List;

@ValidShare
public class ShareReq {
    private Integer documentId;
    private Integer folderId;
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

    public Integer getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Integer documentId) {
        this.documentId = documentId;
    }

    public Integer getFolderId() {
        return folderId;
    }

    public void setFolderId(Integer folderId) {
        this.folderId = folderId;
    }

    public List<UserShareDTO> getShares() {
        return shares;
    }

    public void setShares(List<UserShareDTO> shares) {
        this.shares = shares;
    }
}
