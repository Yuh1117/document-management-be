package com.vpgh.dms.model.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "folders")
public class Folder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;
    private Boolean inheritPermissions;
    private Boolean isDeleted;

    private Instant createdAt;
    private Instant updatedAt;

    @OneToMany(mappedBy = "folder", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Document> documents;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Folder parent;
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Folder> folders;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;
    @ManyToOne
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @OneToMany(mappedBy = "folder", fetch = FetchType.LAZY)
    private Set<FolderPermission> folderPermissions;

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

    public Set<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(Set<Document> documents) {
        this.documents = documents;
    }

    public Folder getParent() {
        return parent;
    }

    public void setParent(Folder parent) {
        this.parent = parent;
    }

    public Set<Folder> getFolders() {
        return folders;
    }

    public void setFolders(Set<Folder> folders) {
        this.folders = folders;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Set<FolderPermission> getFolderPermissions() {
        return folderPermissions;
    }

    public void setFolderPermissions(Set<FolderPermission> folderPermissions) {
        this.folderPermissions = folderPermissions;
    }
}
