package com.vpgh.dms.model;

import com.vpgh.dms.util.annotation.ValidGroup;
import jakarta.validation.constraints.NotBlank;

@ValidGroup
public class UserGroupDTO {
    private Integer id;
    @NotBlank(message = "Tên không được để trống")
    private String name;
    private String description;

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
}
