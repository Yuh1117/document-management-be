package com.vpgh.dms.model.entity;

import com.vpgh.dms.model.FullAuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "system_settings")
public class SystemSetting extends FullAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, name = "`key`", unique = true)
    @NotBlank(message = "Key không được để trống!")
    private String key;
    @Column(nullable = false)
    @NotBlank(message = "Giá trị không được để trống!")
    private String value;
    private String description;

    public SystemSetting() {

    }

    public SystemSetting(String key, String value, String description) {
        this.key = key;
        this.value = value;
        this.description = description;
    }

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
