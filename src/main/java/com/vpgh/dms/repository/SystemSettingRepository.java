package com.vpgh.dms.repository;

import com.vpgh.dms.model.entity.SystemSetting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, Integer> {
    SystemSetting findByKey(String key);

    Page<SystemSetting> findAll(Specification<SystemSetting> specification, Pageable pageable);

    SystemSetting save(SystemSetting setting);

    boolean existsByKeyAndIdNot(String key, Integer id);

    Optional<SystemSetting> findById(Integer integer);

    void deleteById(Integer id);

    long count();
}
