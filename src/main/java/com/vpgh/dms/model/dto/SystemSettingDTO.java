package com.vpgh.dms.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vpgh.dms.util.annotation.ValidSetting;
import jakarta.validation.constraints.NotBlank;

@ValidSetting
public class SystemSettingDTO {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer id;
    @NotBlank(message = "Key không được để trống!")
    private String key;
    @NotBlank(message = "Giá trị không được để trống!")
    private String value;
    private String description;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
