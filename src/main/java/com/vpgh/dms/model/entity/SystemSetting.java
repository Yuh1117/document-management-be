package com.vpgh.dms.model.entity;

import java.util.UUID;

import com.vpgh.dms.model.FullAuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "system_settings")
public class SystemSetting extends FullAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, name = "`key`", unique = true)
    @NotBlank(message = "{validation.setting.key.notBlank}")
    private String key;
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    @NotBlank(message = "{validation.setting.value.notBlank}")
    private String value;
    private String description;

    public SystemSetting() {

    }

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
