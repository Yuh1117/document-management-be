package com.vpgh.dms.repository;

import com.vpgh.dms.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Role findByName(String name);

    boolean existsById(Integer id);

    Optional<Role> findById(Integer id);

}
