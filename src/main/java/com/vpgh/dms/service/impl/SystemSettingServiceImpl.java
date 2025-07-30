package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.dto.SystemSettingDTO;
import com.vpgh.dms.model.entity.SystemSetting;
import com.vpgh.dms.repository.SystemSettingRepository;
import com.vpgh.dms.service.SystemSettingService;
import com.vpgh.dms.service.specification.SystemSettingSpecification;
import com.vpgh.dms.util.PageSize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SystemSettingServiceImpl implements SystemSettingService {

    @Autowired
    private SystemSettingRepository systemSettingRepository;

    @Override
    public SystemSetting getSettingByKey(String key) {
        return this.systemSettingRepository.findByKey(key);
    }

    @Override
    public Page<SystemSetting> getAllSettings(Map<String, String> params) {
        int page = Integer.parseInt(params.get("page"));
        String kw = params.get("kw");

        Pageable pageable = PageRequest.of(page - 1, PageSize.ROLE_PAGE_SIZE.getSize());
        Specification<SystemSetting> combinedSpec = Specification.allOf();
        if (kw != null && !kw.isEmpty()) {
            Specification<SystemSetting> spec = SystemSettingSpecification.filterByKeyword(params.get("kw"));
            combinedSpec = combinedSpec.and(spec);
        }

        return this.systemSettingRepository.findAll(combinedSpec, pageable);
    }

    @Override
    public SystemSetting save(SystemSetting setting) {
        return this.systemSettingRepository.save(setting);
    }

    @Override
    public SystemSetting handleCreateSetting(SystemSettingDTO dto) {
        SystemSetting setting = new SystemSetting();
        setting.setKey(dto.getKey());
        setting.setValue(dto.getValue());
        setting.setDescription(dto.getDescription());

        return save(setting);
    }

    @Override
    public boolean existsByKeyAndIdNot(String key, Integer id) {
        return this.systemSettingRepository.existsByKeyAndIdNot(key, id);
    }

    @Override
    public SystemSetting getSettingById(Integer id) {
        Optional<SystemSetting> setting = this.systemSettingRepository.findById(id);
        return setting.isPresent() ? setting.get() : null;
    }

    @Override
    public SystemSetting handleUpdateSetting(Integer id, SystemSettingDTO dto) {
        SystemSetting setting = getSettingById(id);
        if (setting != null) {
            setting.setKey(dto.getKey());
            setting.setValue(dto.getValue());
            setting.setDescription(dto.getDescription());
            return save(setting);
        }

        return null;
    }

    @Override
    public void deleteSettingById(Integer id) {
        this.systemSettingRepository.deleteById(id);
    }

    @Override
    public long count() {
        return this.systemSettingRepository.count();
    }

    @Override
    public List<SystemSetting> saveAll(List<SystemSetting> settings) {
        return this.systemSettingRepository.saveAll(settings);
    }
}
