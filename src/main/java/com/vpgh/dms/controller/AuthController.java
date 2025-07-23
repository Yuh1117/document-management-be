package com.vpgh.dms.controller;

import com.vpgh.dms.model.entity.Role;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.model.dto.request.UserLoginReqDTO;
import com.vpgh.dms.model.dto.request.UserSignupDTO;
import com.vpgh.dms.model.dto.response.UserLoginResDTO;
import com.vpgh.dms.model.dto.response.UserResDTO;
import com.vpgh.dms.service.RoleService;
import com.vpgh.dms.service.UserService;
import com.vpgh.dms.util.JwtUtil;
import com.vpgh.dms.util.annotation.ApiMessage;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
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
        UserLoginResDTO resUser = new UserLoginResDTO();
        UserResDTO userLogin = new UserResDTO(currentUser);
        resUser.setUser(userLogin);

        String jwtToken = this.jwtUtil.createToken(authentication);
        resUser.setAccessToken(jwtToken);

        return ResponseEntity.status(HttpStatus.OK).body(resUser);
    }

    @PostMapping("/signup")
    @ApiMessage(message = "Đăng ký")
    public ResponseEntity<UserResDTO> signup(@ModelAttribute @Valid UserSignupDTO user) {
        User nuser = new User();
        nuser.setFirstName(user.getFirstName());
        nuser.setLastName(user.getLastName());
        nuser.setEmail(user.getEmail());
        nuser.setPassword(user.getPassword());
        nuser.setFile(user.getFile());

        Role role = this.roleService.getRoleByName("USER");
        nuser.setRole(role);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UserResDTO(this.userService.save(nuser)));
    }
}
