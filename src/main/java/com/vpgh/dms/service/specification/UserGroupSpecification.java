package com.vpgh.dms.service.specification;

import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.model.entity.UserGroup;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class UserGroupSpecification {
    public static Specification<UserGroup> filterByKeyword(String kw) {
        return (root, query, cb) -> {
            Predicate namePredicate = cb.like(cb.lower(root.get("key")), "%" + kw.toLowerCase() + "%");

            return cb.or(namePredicate);
        };
    }

    public static Specification<UserGroup> hasCreatedBy(User user) {
        return (root, query, cb) -> cb.equal(root.get("createdBy"), user);
    }
}
