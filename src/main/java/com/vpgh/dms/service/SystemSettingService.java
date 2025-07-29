package com.vpgh.dms.service;

import com.vpgh.dms.model.entity.SystemSetting;

public interface SystemSettingService {
    SystemSetting getSettingByKey(String key);
}
