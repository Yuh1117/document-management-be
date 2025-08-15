package com.vpgh.dms.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vpgh.dms.model.constant.MemberEnum;

public class MemberDTO {
    @JsonIgnore
    private Integer id;
    private String email;
    private MemberEnum role;

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

    public MemberEnum getRole() {
        return role;
    }

    public void setRole(MemberEnum role) {
        this.role = role;
    }
}
