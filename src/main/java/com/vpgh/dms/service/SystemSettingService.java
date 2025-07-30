package com.vpgh.dms.service;

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

    boolean existsByKeyAndIdNot(String key, Integer id);

    SystemSetting getSettingById(Integer id);

    SystemSetting handleUpdateSetting(Integer id, SystemSettingDTO dto);

    void deleteSettingById(Integer id);

    long count();

    List<SystemSetting> saveAll(List<SystemSetting> settings);
}
