package com.vpgh.dms.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vpgh.dms.model.FullAuditableEntity;
import com.vpgh.dms.model.constant.ProcessingStatus;
import com.vpgh.dms.model.constant.StorageType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

@Entity
@Table(name = "documents")
public class Document extends FullAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    @NotBlank(message = "Tên không được để trống.")
    private String name;
    @Column(columnDefinition = "LONGTEXT", nullable = false)
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
    private Boolean isEncrypted = false;
    @Enumerated(EnumType.STRING)
    private StorageType storageType;
    private Boolean isDeleted = false;

    @Enumerated(EnumType.STRING)
    private ProcessingStatus processingStatus;
    private Integer ocrQualityScore;
    @Column(columnDefinition = "TEXT")
    private String processingError;

    @ManyToOne
    @JoinColumn(name = "folder_id")
    private Folder folder;

    @OneToMany(mappedBy = "document", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<DocumentShare> documentShares;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "document_tag_assignments", joinColumns = @JoinColumn(name = "document_id"), inverseJoinColumns = @JoinColumn(name = "document_tag_id"))
    @JsonIgnore
    private Set<DocumentTag> tags;

    @OneToMany(mappedBy = "document", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<DocumentVersion> versions;

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

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public ProcessingStatus getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(ProcessingStatus processingStatus) {
        this.processingStatus = processingStatus;
    }

    public Integer getOcrQualityScore() {
        return ocrQualityScore;
    }

    public void setOcrQualityScore(Integer ocrQualityScore) {
        this.ocrQualityScore = ocrQualityScore;
    }

    public String getProcessingError() {
        return processingError;
    }

    public void setProcessingError(String processingError) {
        this.processingError = processingError;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public Set<DocumentShare> getDocumentShares() {
        return documentShares;
    }

    public void setDocumentShares(Set<DocumentShare> documentShares) {
        this.documentShares = documentShares;
    }

    public Set<DocumentTag> getTags() {
        return tags;
    }

    public void setTags(Set<DocumentTag> tags) {
        this.tags = tags;
    }

    public Set<DocumentVersion> getVersions() {
        return versions;
    }

    public void setVersions(Set<DocumentVersion> versions) {
        this.versions = versions;
    }

}
