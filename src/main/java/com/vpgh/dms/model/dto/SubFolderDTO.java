package com.vpgh.dms.model.dto;

import java.time.Instant;

public class SubFolderDTO {
    private Integer id;
    private String name;
    private Boolean inheritPermissions;
    private Boolean isDeleted;

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
