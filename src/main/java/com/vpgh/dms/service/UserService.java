package com.vpgh.dms.service;

import com.vpgh.dms.model.entity.User;
import org.springframework.data.domain.Page;

import java.awt.print.Pageable;
import java.util.Map;

public interface UserService {
    User save(User user);

    boolean existsByEmail(String email);

    User getUserByEmail(String email);

    Page<User> getAllUsers(Map<String, String> params);

    User getUserById(Integer id);

    void deleteUserById(Integer id);

    boolean existsByEmailAndIdNot(String email, Integer id);
}
