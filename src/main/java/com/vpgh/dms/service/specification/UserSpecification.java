package com.vpgh.dms.service.specification;

import com.vpgh.dms.model.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {
    public static Specification<User> filterByKeyword(String kw) {
        return (root, query, cb) -> {
            Predicate firstNamePredicate = cb.like(cb.lower(root.get("firstName")), "%" + kw.toLowerCase() + "%");
            Predicate lastNamePredicate = cb.like(cb.lower(root.get("lastName")), "%" + kw.toLowerCase() + "%");

            return cb.or(firstNamePredicate, lastNamePredicate);
        };
    }
}
