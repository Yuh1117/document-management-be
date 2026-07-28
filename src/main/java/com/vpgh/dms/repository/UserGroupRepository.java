package com.vpgh.dms.repository;
import java.util.UUID;

import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.model.entity.UserGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserGroupRepository extends JpaRepository<UserGroup, UUID> {
    boolean existsByNameAndCreatedByAndIdNot(String name, User createdBy, UUID id);

    Page<UserGroup> findAll(Specification<UserGroup> specification, Pageable pageable);

    Optional<UserGroup> findById(UUID id);

    void deleteById(UUID id);
}