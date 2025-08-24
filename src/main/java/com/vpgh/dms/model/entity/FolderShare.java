package com.vpgh.dms.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.vpgh.dms.model.FullAuditableEntity;
import com.vpgh.dms.model.UserDTOSerializer;
import com.vpgh.dms.model.constant.ShareType;
import jakarta.persistence.*;

@Entity
@Table(name = "folder_shares")
public class FolderShare extends FullAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private ShareType shareType;

    @ManyToOne
    @JoinColumn(name = "folder_id")
    private Folder folder;
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonSerialize(using = UserDTOSerializer.class)
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

    public ShareType getShareType() {
        return shareType;
    }

    public void setShareType(ShareType shareType) {
        this.shareType = shareType;
    }

    public UserGroup getGroup() {
        return group;
    }

    public void setGroup(UserGroup group) {
        this.group = group;
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

}
