package com.vpgh.dms.model.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "user_groups")
public class UserGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private String name;
    private String description;

    private Instant createdAt;
    private Instant updatedAt;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;
    @ManyToOne
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    private Set<UserGroupMember> members;

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    private Set<FolderPermission> folderPermissions;

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    private Set<DocumentPermission> documentPermissions;

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

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

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Set<UserGroupMember> getMembers() {
        return members;
    }

    public void setMembers(Set<UserGroupMember> members) {
        this.members = members;
    }

}
