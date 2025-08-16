package com.vpgh.dms.model.dto;

import java.time.Instant;
import java.util.Set;

public class FolderDTO {
    private Integer id;
    private String name;
    private Boolean inheritPermissions;
    private Boolean isDeleted;
    private Set<DocumentDTO> documents;
    private Set<SubFolderDTO> folders;

    private UserDTO createdBy;
    private UserDTO updatedBy;
    private Instant createdAt;
    private Instant updatedAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getInheritPermissions() {
        return inheritPermissions;
    }

    public void setInheritPermissions(Boolean inheritPermissions) {
        this.inheritPermissions = inheritPermissions;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public Set<DocumentDTO> getDocuments() {
        return documents;
    }

    public void setDocuments(Set<DocumentDTO> documents) {
        this.documents = documents;
    }

    public Set<SubFolderDTO> getFolders() {
        return folders;
    }

    public void setFolders(Set<SubFolderDTO> folders) {
        this.folders = folders;
    }

    public UserDTO getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserDTO createdBy) {
        this.createdBy = createdBy;
    }

    public UserDTO getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(UserDTO updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
