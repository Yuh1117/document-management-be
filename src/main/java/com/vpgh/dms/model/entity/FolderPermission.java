package com.vpgh.dms.model.entity;

import com.vpgh.dms.model.FullAuditableEntity;
import com.vpgh.dms.model.constant.PermissionType;
import jakarta.persistence.*;

@Entity
@Table(name = "folder_permissions")
public class FolderPermission extends FullAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private PermissionType permissionType;

    @ManyToOne
    @JoinColumn(name = "folder_id")
    private Folder folder;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne
    @JoinColumn(name = "group_id")
    private UserGroup group;

    @ManyToOne
    @JoinColumn(name = "created_by")

    private User createdBy;
    @ManyToOne
    @JoinColumn(name = "updated_by")

    private User updatedBy;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public PermissionType getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(PermissionType permissionType) {
        this.permissionType = permissionType;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public User getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public User getUpdatedBy() {
        return updatedBy;
    }

    @Override
    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }
}
