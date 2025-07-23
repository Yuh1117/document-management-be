package com.vpgh.dms.repository;

import com.vpgh.dms.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User save(User user);

    boolean existsByEmail(String email);

    User findByEmail(String email);
}
