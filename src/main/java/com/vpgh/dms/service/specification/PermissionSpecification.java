package com.vpgh.dms.service.specification;

import com.vpgh.dms.model.entity.Permission;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class PermissionSpecification {
    public static Specification<Permission> filterByKeyword(String kw) {
        return (root, query, cb) -> {
            Predicate namePredicate = cb.like(cb.lower(root.get("name")), "%" + kw.toLowerCase() + "%");

            return cb.or(namePredicate);
        };
    }
}
