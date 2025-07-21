package com.vpgh.dms.service;

import com.vpgh.dms.model.entity.User;
import org.springframework.stereotype.Service;

public interface UserService {
    User save(User user);

    boolean existsByEmail(String email);
}
