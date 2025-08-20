package com.vpgh.dms.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vpgh.dms.model.FullAuditableEntity;
import jakarta.persistence.*;

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

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserGroupMember> members;

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<FolderShare> folderShares;

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<DocumentShare> documentShares;

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

}
