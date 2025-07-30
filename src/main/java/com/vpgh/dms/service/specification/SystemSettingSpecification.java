package com.vpgh.dms.service.specification;

import com.vpgh.dms.model.entity.SystemSetting;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;


public class SystemSettingSpecification {
    public static Specification<SystemSetting> filterByKeyword(String kw) {
        return (root, query, cb) -> {
            Predicate namePredicate = cb.like(cb.lower(root.get("key")), "%" + kw.toLowerCase() + "%");

            return cb.or(namePredicate);
        };
    }
}
