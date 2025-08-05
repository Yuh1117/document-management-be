package com.vpgh.dms.service.specification;

import com.vpgh.dms.model.entity.Folder;
import com.vpgh.dms.model.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class FolderSpecification {
    public static Specification<Folder> filterByKeyword(String kw, User user) {
        return (root, query, cb) -> {
            Predicate namePredicate = cb.like(cb.lower(root.get("name")), "%" + kw.toLowerCase() + "%");
            Predicate createdByPredicate = cb.equal(root.get("createdBy"), user);
            Predicate notDeletedPredicate = cb.isFalse(root.get("isDeleted"));

            return cb.and(createdByPredicate, notDeletedPredicate, namePredicate);
        };
    }
}
