package com.vpgh.dms.repository;
import java.util.UUID;

import com.vpgh.dms.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    User save(User user);

    boolean existsByEmail(String email);

    User findByEmail(String email);

    Page<User> findAll(Specification<User> specification, Pageable pageable);

    Optional<User> findById(UUID id);

    List<User> findByIdIn(List<UUID> ids);

    void deleteById(UUID id);

    boolean existsByEmailAndIdNot(String email, UUID id);

    long count();
}
