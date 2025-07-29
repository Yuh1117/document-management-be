package com.vpgh.dms.repository;

import com.vpgh.dms.model.entity.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, Integer> {
    SystemSetting findByKey(String key);
}
