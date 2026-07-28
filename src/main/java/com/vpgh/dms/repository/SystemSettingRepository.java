package com.vpgh.dms.repository;
import java.util.UUID;

import com.vpgh.dms.model.entity.SystemSetting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, UUID> {
    SystemSetting findByKey(String key);

    Page<SystemSetting> findAll(Specification<SystemSetting> specification, Pageable pageable);

    SystemSetting save(SystemSetting setting);

    boolean existsByKeyAndIdNot(String key, UUID id);

    Optional<SystemSetting> findById(UUID UUID);

    void deleteById(UUID id);

    long count();
}
