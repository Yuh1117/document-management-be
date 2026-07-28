package com.vpgh.dms.service;
import java.util.UUID;

import com.vpgh.dms.model.dto.UserDTO;
import com.vpgh.dms.model.entity.User;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface UserService {
    User save(User user);

    boolean existsByEmail(String email);

    User getUserByEmail(String email);

    Page<User> getAllUsers(Map<String, String> params);

    User getUserById(UUID id);

    List<User> getAllByIds(List<UUID> ids);

    void deleteUserById(UUID id);

    boolean existsByEmailAndIdNot(String email, UUID id);

    User handleCreateUser(UserDTO dto);

    User handleUpdateUser(User user, UserDTO dto);

    UserDTO convertUserToUserDTO(User user);

    long count();

}
