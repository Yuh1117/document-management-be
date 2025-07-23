package com.vpgh.dms.controller;

import com.vpgh.dms.model.dto.UserDTO;
import com.vpgh.dms.model.entity.Role;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.model.dto.response.UserResDTO;
import com.vpgh.dms.service.RoleService;
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
    @Autowired
    private RoleService roleService;

    @PostMapping(path = "/users")
    @ApiMessage(message = "Tạo mới user")
    public ResponseEntity<UserResDTO> create(@ModelAttribute @Valid UserDTO user) {
        User nuser = new User();
        nuser.setFirstName(user.getFirstName());
        nuser.setLastName(user.getLastName());
        nuser.setEmail(user.getEmail());
        nuser.setPassword(user.getPassword());
        nuser.setFile(user.getFile());

        Role role = this.roleService.getRoleById(user.getRoleId());
        nuser.setRole(role);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UserResDTO(this.userService.save(nuser)));
    }

}
