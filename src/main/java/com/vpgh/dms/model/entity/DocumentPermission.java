package com.vpgh.dms.model.entity;

import com.vpgh.dms.model.FullAuditableEntity;
import com.vpgh.dms.model.constant.PermissionType;
import jakarta.persistence.*;

@Entity
@Table(name = "document_permissions")
public class DocumentPermission extends FullAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private PermissionType permissionType;

    @ManyToOne
    @JoinColumn(name = "document_id")
    private Document document;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne
    @JoinColumn(name = "group_id")
    private UserGroup group;

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

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
