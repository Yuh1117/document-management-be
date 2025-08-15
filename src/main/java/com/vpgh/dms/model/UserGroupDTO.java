package com.vpgh.dms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vpgh.dms.model.dto.MemberDTO;
import com.vpgh.dms.util.annotation.ValidGroup;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@ValidGroup
public class UserGroupDTO {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer id;
    @NotBlank(message = "Tên không được để trống")
    private String name;
    private String description;
    List<MemberDTO> members;

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

    public List<MemberDTO> getMembers() {
        return members;
    }

    public void setMembers(List<MemberDTO> members) {
        this.members = members;
    }
}
