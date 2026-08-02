package com.vpgh.dms.model.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vpgh.dms.util.annotation.ValidSetting;
import jakarta.validation.constraints.NotBlank;

@ValidSetting
public class SystemSettingDTO {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID id;
    @NotBlank(message = "{validation.setting.key.notBlank}")
    private String key;
    @NotBlank(message = "{validation.setting.value.notBlank}")
    private String value;
    private String description;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
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
