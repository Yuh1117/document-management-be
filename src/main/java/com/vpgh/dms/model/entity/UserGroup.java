package com.vpgh.dms.model.entity;

import com.vpgh.dms.model.FullAuditableEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Set;

@Entity
@Table(name = "user_groups")
public class UserGroup extends FullAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private String name;
    private String description;

    @ManyToOne
    @JoinColumn(name = "created_by")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User createdBy;
    @ManyToOne
    @JoinColumn(name = "updated_by")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User updatedBy;

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    private Set<UserGroupMember> members;

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    private Set<FolderPermission> folderPermissions;

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    private Set<DocumentPermission> documentPermissions;

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

    public Set<UserGroupMember> getMembers() {
        return members;
    }

    public void setMembers(Set<UserGroupMember> members) {
        this.members = members;
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
