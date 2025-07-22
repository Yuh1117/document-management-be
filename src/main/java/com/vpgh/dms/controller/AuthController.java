package com.vpgh.dms.controller;

import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.model.request.UserLoginReqDTO;
import com.vpgh.dms.model.response.UserLoginResDTO;
import com.vpgh.dms.service.UserService;
import com.vpgh.dms.util.JwtUtil;
import com.vpgh.dms.util.annotation.ApiMessage;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        UserLoginResDTO.UserLogin userLogin = new UserLoginResDTO.UserLogin(currentUser.getId(), currentUser.getEmail(),
                currentUser.getFirstName(), currentUser.getLastName(), currentUser.getAvatar());
        resUser.setUser(userLogin);

        String jwtToken = this.jwtUtil.createToken(authentication);
        resUser.setAccessToken(jwtToken);

        return ResponseEntity.status(HttpStatus.OK).body(resUser);
    }
}
