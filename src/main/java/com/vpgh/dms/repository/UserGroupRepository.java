package com.vpgh.dms.repository;

import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.model.entity.UserGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserGroupRepository extends JpaRepository<UserGroup, Integer> {
    boolean existsByNameAndCreatedByAndIdNot(String name, User createdBy, Integer id);

    Page<UserGroup> findAll(Specification<UserGroup> specification, Pageable pageable);

    Optional<UserGroup> findById(Integer id);

    void deleteById(Integer id);
}
