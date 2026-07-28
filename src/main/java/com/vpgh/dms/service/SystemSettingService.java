package com.vpgh.dms.service;
import java.util.UUID;

import com.vpgh.dms.model.dto.SystemSettingDTO;
import com.vpgh.dms.model.entity.SystemSetting;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface SystemSettingService {
    SystemSetting getSettingByKey(String key);

    Page<SystemSetting> getAllSettings(Map<String, String> params);

    SystemSetting save(SystemSetting setting);

    SystemSetting handleCreateSetting(SystemSettingDTO dto);

    boolean existsByKeyAndIdNot(String key, UUID id);

    SystemSetting getSettingById(UUID id);

    SystemSetting handleUpdateSetting(SystemSetting setting, SystemSettingDTO dto);

    void deleteSettingById(UUID id);

    long count();

    List<SystemSetting> saveAll(List<SystemSetting> settings);
}