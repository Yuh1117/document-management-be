package com.vpgh.dms.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vpgh.dms.model.FullAuditableEntity;
import com.vpgh.dms.model.constant.StorageType;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Set;

@Entity
@Table(name = "documents")
public class Document extends FullAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;
    private String description;
    @Column(nullable = false)
    private String originalFilename;
    @Column(nullable = false)
    private String storedFilename;
    @Column(nullable = false)
    private String filePath;
    @Column(nullable = false)
    private Double fileSize;
    @Column(nullable = false)
    private String mimeType;
    @Column(nullable = false)
    private String fileHash;
    private Boolean isEncrypted;
    @Enumerated(EnumType.STRING)
    private StorageType storageType;
    private String extractedText;
    private Boolean isDeleted;

    @ManyToOne
    @JoinColumn(name = "created_by")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User createdBy;
    @ManyToOne
    @JoinColumn(name = "updated_by")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User updatedBy;

    @ManyToOne
    @JoinColumn(name = "folder_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Folder folder;

    @OneToMany(mappedBy = "document", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<DocumentPermission> documentPermissions;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "document_tag_assignments", joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "document_tag_id"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Set<DocumentTag> tags;

    @OneToMany(mappedBy = "document", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<DocumentSearchIndex> documentSearchIndices;

    @OneToMany(mappedBy = "document", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<DocumentVersion> versions;

    @OneToMany(mappedBy = "document", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<AccessLog> accessLogs;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getStoredFilename() {
        return storedFilename;
    }

    public void setStoredFilename(String storedFilename) {
        this.storedFilename = storedFilename;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Double getFileSize() {
        return fileSize;
    }

    public void setFileSize(Double fileSize) {
        this.fileSize = fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public Boolean getEncrypted() {
        return isEncrypted;
    }

    public void setEncrypted(Boolean encrypted) {
        isEncrypted = encrypted;
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public void setStorageType(StorageType storageType) {
        this.storageType = storageType;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public Set<DocumentPermission> getDocumentPermissions() {
        return documentPermissions;
    }

    public void setDocumentPermissions(Set<DocumentPermission> documentPermissions) {
        this.documentPermissions = documentPermissions;
    }

    public Set<DocumentTag> getTags() {
        return tags;
    }

    public void setTags(Set<DocumentTag> tags) {
        this.tags = tags;
    }

    public Set<DocumentSearchIndex> getDocumentSearchIndices() {
        return documentSearchIndices;
    }

    public void setDocumentSearchIndices(Set<DocumentSearchIndex> documentSearchIndices) {
        this.documentSearchIndices = documentSearchIndices;
    }

    public Set<DocumentVersion> getVersions() {
        return versions;
    }

    public void setVersions(Set<DocumentVersion> versions) {
        this.versions = versions;
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
