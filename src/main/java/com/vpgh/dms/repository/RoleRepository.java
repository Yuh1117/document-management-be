package com.vpgh.dms.repository;
import java.util.UUID;

import com.vpgh.dms.model.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID>, JpaSpecificationExecutor<Role> {
    Role findByName(String name);

    boolean existsById(UUID id);

    Optional<Role> findById(UUID id);

    Page<Role> findAll(Specification<Role> specification, Pageable pageable);

    Role save(Role role);

    boolean existsByNameAndIdNot(String name, UUID id);

    void deleteById(UUID id);

    long count();
}
