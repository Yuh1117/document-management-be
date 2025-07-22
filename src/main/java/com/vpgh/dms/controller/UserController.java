package com.vpgh.dms.controller;

import com.vpgh.dms.exception.UniqueConstraintException;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.model.response.UserResDTO;
import com.vpgh.dms.service.UserService;
import com.vpgh.dms.util.annotation.ApiMessage;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping(path = "/users")
    @ApiMessage(message = "Tạo mới user")
    public ResponseEntity<UserResDTO> create(@ModelAttribute @Valid User user) throws UniqueConstraintException {
        if (userService.existsByEmail(user.getEmail())) {
            throw new UniqueConstraintException("Email đã tồn tại!");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return ResponseEntity.status(HttpStatus.CREATED).body(new UserResDTO(userService.save(user)));
    }

}
