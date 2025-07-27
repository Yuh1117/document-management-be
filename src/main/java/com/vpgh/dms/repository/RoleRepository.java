package com.vpgh.dms.repository;

import com.vpgh.dms.model.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer>, JpaSpecificationExecutor<Role> {
    Role findByName(String name);

    boolean existsById(Integer id);

    Optional<Role> findById(Integer id);

    Page<Role> findAll(Specification<Role> specification, Pageable pageable);

    Role save(Role role);

    boolean existsByNameAndIdNot(String name, Integer id);

    void deleteById(Integer id);

}
