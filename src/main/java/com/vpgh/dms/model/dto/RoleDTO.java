package com.vpgh.dms.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vpgh.dms.model.entity.Permission;
import com.vpgh.dms.util.annotation.ValidRole;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@ValidRole
public class RoleDTO {
    private Integer id;
    @NotBlank(message = "Tên không được để trống")
    private String name;
    private String description;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<Permission> permissions;

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

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }
}
