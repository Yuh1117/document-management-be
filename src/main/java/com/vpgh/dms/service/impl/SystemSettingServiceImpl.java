package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.entity.SystemSetting;
import com.vpgh.dms.repository.SystemSettingRepository;
import com.vpgh.dms.service.SystemSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SystemSettingServiceImpl implements SystemSettingService {

    @Autowired
    private SystemSettingRepository systemSettingRepository;

    @Override
    public SystemSetting getSettingByKey(String key) {
        return this.systemSettingRepository.findByKey(key);
    }
}
