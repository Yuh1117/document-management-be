package com.vpgh.dms.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private final UserService userService;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtUtil jwtUtil;
    private final JwtDecoder jwtDecoder;
    private final RoleService roleService;
    @Value("${google.client-id}")
    private String clientId;
    @Value("${google.client-secret}")
    private String clientSecret;
    @Value("${google.token-url}")
    private String googleTokenUrl;

    public AuthController(UserService userService, AuthenticationManagerBuilder authenticationManagerBuilder,
            JwtUtil jwtUtil, JwtDecoder jwtDecoder, RoleService roleService) {
        this.userService = userService;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.jwtUtil = jwtUtil;
        this.jwtDecoder = jwtDecoder;
        this.roleService = roleService;
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/api/auth");
        cookie.setMaxAge((int) jwtUtil.refreshTokenExpiration);
        response.addCookie(cookie);
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null)
            return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> REFRESH_TOKEN_COOKIE.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    @PostMapping("/login")
    @ApiMessage(key = "api.auth.login", message = "Sign in")
    public ResponseEntity<UserLoginResDTO> login(@RequestBody @Valid UserLoginReqDTO user,
            HttpServletResponse response) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                user.getEmail(), user.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject()
                .authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User currentUser = this.userService.getUserByEmail(user.getEmail());
        UserDTO userRes = this.userService.convertUserToUserDTO(currentUser);

        String accessToken = this.jwtUtil.createToken(userRes);
        String refreshToken = this.jwtUtil.createRefreshToken(userRes);

        setRefreshTokenCookie(response, refreshToken);

        UserLoginResDTO userLoginRes = new UserLoginResDTO();
        userLoginRes.setUser(userRes);
        userLoginRes.setAccessToken(accessToken);

        return ResponseEntity.status(HttpStatus.OK).body(userLoginRes);
    }

    @PostMapping("/auth/refresh")
    @ApiMessage(key = "api.auth.refresh", message = "Refresh token")
    public ResponseEntity<UserLoginResDTO> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Jwt jwt = jwtDecoder.decode(refreshToken);
            String email = jwt.getSubject();

            User currentUser = userService.getUserByEmail(email);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            UserDTO userRes = userService.convertUserToUserDTO(currentUser);
            String newAccessToken = jwtUtil.createToken(userRes);
            String newRefreshToken = jwtUtil.createRefreshToken(userRes);

            setRefreshTokenCookie(response, newRefreshToken);

            UserLoginResDTO userLoginRes = new UserLoginResDTO();
            userLoginRes.setUser(userRes);
            userLoginRes.setAccessToken(newAccessToken);

            return ResponseEntity.ok(userLoginRes);
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/auth/logout")
    @ApiMessage(key = "api.auth.logout", message = "Logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        clearRefreshTokenCookie(response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/signup")
    @ApiMessage(key = "api.auth.register", message = "Register")
    public ResponseEntity<UserDTO> signup(@ModelAttribute @Valid UserSignupReqDTO user) {
        User nuser = new User();
        nuser.setFirstName(user.getFirstName());
        nuser.setLastName(user.getLastName());
        nuser.setEmail(user.getEmail());
        nuser.setPassword(user.getPassword());
        nuser.setFile(user.getFile());

        Role role = this.roleService.getRoleByName("ROLE_USER");
        nuser.setRole(role);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.userService.convertUserToUserDTO(this.userService.save(nuser)));
    }

    @GetMapping("/secure/profile")
    @ApiMessage(key = "api.auth.profile", message = "Get profile")
    public ResponseEntity<UserDTO> getProfile() {
        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();

        return ResponseEntity.status(HttpStatus.OK).body(this.userService.convertUserToUserDTO(currentUser));
    }

    @PostMapping("/auth/google")
    @ApiMessage(key = "api.auth.google", message = "Sign in with Google")
    public ResponseEntity<?> loginWithGoogle(@RequestBody Map<String, String> body, HttpServletResponse response) {
        try {
            String code = body.get("code");

            WebClient webClient = WebClient.create("https://oauth2.googleapis.com");

            Map<String, Object> tokenRes = webClient.post()
                    .uri("/token")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .body(BodyInserters.fromFormData("code", code)
                            .with("client_id", clientId)
                            .with("client_secret", clientSecret)
                            .with("redirect_uri", "postmessage")
                            .with("grant_type", "authorization_code"))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    })
                    .block();

            String token = (String) tokenRes.get("id_token");

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance()).setAudience(Collections.singletonList(clientId)).build();

            GoogleIdToken idToken = verifier.verify(token);

            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String firstName = payload.get("given_name") != null ? (String) payload.get("given_name") : "";
                String lastName = payload.get("family_name") != null ? (String) payload.get("family_name") : "";

                User user = userService.getUserByEmail(email);
                if (user == null) {
                    return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                            "email", email,
                            "firstName", firstName,
                            "lastName", lastName));
                }

                try {
                    UserDTO userRes = this.userService.convertUserToUserDTO(user);
                    String accessToken = this.jwtUtil.createToken(userRes);
                    String refreshToken = this.jwtUtil.createRefreshToken(userRes);

                    setRefreshTokenCookie(response, refreshToken);

                    UserLoginResDTO userLoginRes = new UserLoginResDTO();
                    userLoginRes.setUser(userRes);
                    userLoginRes.setAccessToken(accessToken);

                    return ResponseEntity.status(HttpStatus.OK).body(userLoginRes);
                } catch (Exception e) {
                    return ResponseEntity.status(500).body("Lỗi khi tạo JWT");
                }

            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error");
        }
    }
}
