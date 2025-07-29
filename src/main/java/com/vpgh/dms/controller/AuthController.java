package com.vpgh.dms.controller;

import com.vpgh.dms.model.dto.UserDTO;
import com.vpgh.dms.model.entity.Role;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.model.dto.request.UserLoginReqDTO;
import com.vpgh.dms.model.dto.request.UserSignupReqDTO;
import com.vpgh.dms.model.dto.response.UserLoginResDTO;
import com.vpgh.dms.service.RoleService;
import com.vpgh.dms.service.UserService;
import com.vpgh.dms.util.JwtUtil;
import com.vpgh.dms.util.SecurityUtil;
import com.vpgh.dms.util.annotation.ApiMessage;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired
    private AuthenticationManagerBuilder authenticationManagerBuilder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RoleService roleService;

    @PostMapping("/login")
    @ApiMessage(message = "Đăng nhập")
    public ResponseEntity<UserLoginResDTO> login(@RequestBody @Valid UserLoginReqDTO user) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                user.getEmail(), user.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject()
                .authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User currentUser = this.userService.getUserByEmail(user.getEmail());
        UserDTO userRes = this.userService.convertUserToUserDTO(currentUser);

        UserLoginResDTO userLoginRes = new UserLoginResDTO();
        userLoginRes.setUser(userRes);

        String jwtToken = this.jwtUtil.createToken(authentication, userRes);
        userLoginRes.setAccessToken(jwtToken);

        return ResponseEntity.status(HttpStatus.OK).body(userLoginRes);
    }

    @PostMapping("/signup")
    @ApiMessage(message = "Đăng ký")
    public ResponseEntity<UserDTO> signup(@ModelAttribute @Valid UserSignupReqDTO user) {
        User nuser = new User();
        nuser.setFirstName(user.getFirstName());
        nuser.setLastName(user.getLastName());
        nuser.setEmail(user.getEmail());
        nuser.setPassword(user.getPassword());
        nuser.setFile(user.getFile());

        Role role = this.roleService.getRoleByName("USER");
        nuser.setRole(role);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.convertUserToUserDTO(this.userService.save(nuser)));
    }

    @GetMapping("/secure/profile")
    @ApiMessage(message = "Lấy profile")
    public ResponseEntity<UserDTO> getProfile() {
        User currentUser = SecurityUtil.getCurrentUser();

        return ResponseEntity.status(HttpStatus.OK).body(this.userService.convertUserToUserDTO(currentUser));
    }
}
