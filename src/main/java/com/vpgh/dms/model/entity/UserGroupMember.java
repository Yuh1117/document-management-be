package com.vpgh.dms.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.vpgh.dms.model.UserDTOSerializer;
import com.vpgh.dms.model.constant.MemberEnum;
import jakarta.persistence.*;

@Entity
@Table(name = "user_group_members")
public class UserGroupMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private MemberEnum role;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonSerialize(using = UserDTOSerializer.class)
    private User user;
    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    @JsonIgnore
    private UserGroup group;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public MemberEnum getRole() {
        return role;
    }

    public void setRole(MemberEnum role) {
        this.role = role;
    }

    public UserGroup getGroup() {
        return group;
    }

    public void setGroup(UserGroup group) {
        this.group = group;
    }
}
