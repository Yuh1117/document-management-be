package com.vpgh.dms.repository;

import com.vpgh.dms.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {
    User save(User user);

    boolean existsByEmail(String email);

    User findByEmail(String email);

    Page<User> findAll(Specification<User> productSpecification, Pageable pageable);

    Optional<User> findById(Integer id);

    void deleteById(Integer id);

    boolean existsByEmailAndIdNot(String email, Integer id);
}
