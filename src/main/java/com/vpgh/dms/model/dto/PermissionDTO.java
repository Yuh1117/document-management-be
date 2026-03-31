package com.vpgh.dms.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vpgh.dms.util.annotation.ValidPermission;
import jakarta.validation.constraints.NotNull;

@ValidPermission
public class PermissionDTO {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer id;
    @NotNull(message = "{validation.permission.name.notNull}")
    private String name;
    @NotNull(message = "{validation.permission.path.notNull}")
    private String apiPath;
    @NotNull(message = "{validation.permission.method.notNull}")
    private String method;
    @NotNull(message = "{validation.permission.module.notNull}")
    private String module;

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

    public String getApiPath() {
        return apiPath;
    }

    public void setApiPath(String apiPath) {
        this.apiPath = apiPath;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }
}
