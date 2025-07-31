package com.vpgh.dms.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vpgh.dms.model.TimestampedEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Entity
@Table(name = "users")
public class User extends TimestampedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    @Email(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Email không hợp lệ!")
    @NotBlank(message = "Email không được để trống")
    private String email;
    @Column(nullable = false)
    @NotBlank(message = "Mật khẩu không được để trống")
    @JsonIgnore
    private String password;
    @Column(nullable = false)
    @NotBlank(message = "Tên không được để trống")
    private String firstName;
    @Column(nullable = false)
    @NotBlank(message = "Họ không được để trống")
    private String lastName;
    private String avatar;

    private Boolean twoFactorEnabled;
    private String twoFactorSecret;
    private Boolean isActive = true;

    @Transient
    @JsonIgnore
    private MultipartFile file;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<SystemSetting> createdSettings;
    @OneToMany(mappedBy = "updatedBy", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<SystemSetting> updatedSettings;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<OTPCode> otpCodes;

    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<UserGroup> createdGroups;
    @OneToMany(mappedBy = "updatedBy", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<UserGroup> updatedGroups;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<UserGroupMember> groups;

    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Folder> createdFolders;
    @OneToMany(mappedBy = "updatedBy", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Folder> updatedFolders;

    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Document> createdDocuments;
    @OneToMany(mappedBy = "updatedBy", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Document> updatedDocuments;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<FolderPermission> folderPermissions;
    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<FolderPermission> createdFolderPermissions;
    @OneToMany(mappedBy = "updatedBy", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<FolderPermission> updatedFolderPermissions;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<DocumentPermission> documentPermissions;
    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<DocumentPermission> createdDocumentPermissions;
    @OneToMany(mappedBy = "updatedBy", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<DocumentPermission> updatedDocumentPermissions;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<AccessLog> accessLogs;

    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<DocumentTag> createdTags;
    @OneToMany(mappedBy = "updatedBy", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<DocumentTag> updatedTags;

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public Set<DocumentTag> getCreatedTags() {
        return createdTags;
    }

    public void setCreatedTags(Set<DocumentTag> createdTags) {
        this.createdTags = createdTags;
    }

    public Set<DocumentTag> getUpdatedTags() {
        return updatedTags;
    }

    public void setUpdatedTags(Set<DocumentTag> updatedTags) {
        this.updatedTags = updatedTags;
    }

    public Set<DocumentPermission> getDocumentPermissions() {
        return documentPermissions;
    }

    public void setDocumentPermissions(Set<DocumentPermission> documentPermissions) {
        this.documentPermissions = documentPermissions;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Boolean getTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public void setTwoFactorEnabled(Boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }

    public String getTwoFactorSecret() {
        return twoFactorSecret;
    }

    public void setTwoFactorSecret(String twoFactorSecret) {
        this.twoFactorSecret = twoFactorSecret;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Set<SystemSetting> getCreatedSettings() {
        return createdSettings;
    }

    public void setCreatedSettings(Set<SystemSetting> createdSettings) {
        this.createdSettings = createdSettings;
    }

    public Set<SystemSetting> getUpdatedSettings() {
        return updatedSettings;
    }

    public void setUpdatedSettings(Set<SystemSetting> updatedSettings) {
        this.updatedSettings = updatedSettings;
    }

    public Set<OTPCode> getOtpCodes() {
        return otpCodes;
    }

    public void setOtpCodes(Set<OTPCode> otpCodes) {
        this.otpCodes = otpCodes;
    }

    public Set<UserGroup> getCreatedGroups() {
        return createdGroups;
    }

    public void setCreatedGroups(Set<UserGroup> createdGroups) {
        this.createdGroups = createdGroups;
    }

    public Set<UserGroup> getUpdatedGroups() {
        return updatedGroups;
    }

    public void setUpdatedGroups(Set<UserGroup> updatedGroups) {
        this.updatedGroups = updatedGroups;
    }

    public Set<UserGroupMember> getGroups() {
        return groups;
    }

    public void setGroups(Set<UserGroupMember> groups) {
        this.groups = groups;
    }

    public Set<Folder> getCreatedFolders() {
        return createdFolders;
    }

    public void setCreatedFolders(Set<Folder> createdFolders) {
        this.createdFolders = createdFolders;
    }

    public Set<Folder> getUpdatedFolders() {
        return updatedFolders;
    }

    public void setUpdatedFolders(Set<Folder> updatedFolders) {
        this.updatedFolders = updatedFolders;
    }

    public Set<Document> getCreatedDocuments() {
        return createdDocuments;
    }

    public void setCreatedDocuments(Set<Document> createdDocuments) {
        this.createdDocuments = createdDocuments;
    }

    public Set<Document> getUpdatedDocuments() {
        return updatedDocuments;
    }

    public void setUpdatedDocuments(Set<Document> updatedDocuments) {
        this.updatedDocuments = updatedDocuments;
    }

    public Set<FolderPermission> getFolderPermissions() {
        return folderPermissions;
    }

    public void setFolderPermissions(Set<FolderPermission> folderPermissions) {
        this.folderPermissions = folderPermissions;
    }

    public Set<FolderPermission> getCreatedFolderPermissions() {
        return createdFolderPermissions;
    }

    public void setCreatedFolderPermissions(Set<FolderPermission> createdFolderPermissions) {
        this.createdFolderPermissions = createdFolderPermissions;
    }

    public Set<FolderPermission> getUpdatedFolderPermissions() {
        return updatedFolderPermissions;
    }

    public void setUpdatedFolderPermissions(Set<FolderPermission> updatedFolderPermissions) {
        this.updatedFolderPermissions = updatedFolderPermissions;
    }

    public Set<DocumentPermission> getCreatedDocumentPermissions() {
        return createdDocumentPermissions;
    }

    public void setCreatedDocumentPermissions(Set<DocumentPermission> createdDocumentPermissions) {
        this.createdDocumentPermissions = createdDocumentPermissions;
    }

    public Set<DocumentPermission> getUpdatedDocumentPermissions() {
        return updatedDocumentPermissions;
    }

    public void setUpdatedDocumentPermissions(Set<DocumentPermission> updatedDocumentPermissions) {
        this.updatedDocumentPermissions = updatedDocumentPermissions;
    }

    public Set<AccessLog> getAccessLogs() {
        return accessLogs;
    }

    public void setAccessLogs(Set<AccessLog> accessLogs) {
        this.accessLogs = accessLogs;
    }
}
