package com.vpgh.dms.util;

import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.service.UserService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {
    @Autowired
    private UserService userService;

    private static UserService staticUserService;

    @PostConstruct
    public void init() {
        staticUserService = userService;
    }

    private static final ThreadLocal<User> currentUser = new ThreadLocal<>();

    public static void setCurrentUser(User user) {
        currentUser.set(user);
    }

    public static User getCurrentUserFromThreadLocal() {
        return currentUser.get();
    }

    public static void clear() {
        currentUser.remove();
    }

    public static User getCurrentUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        String username = extractPrincipal(authentication);

        if (username == null) return null;
        return staticUserService.getUserWithRoleAndPermissions(username);
    }

    private static String extractPrincipal(Authentication authentication) {
        if (authentication == null) return null;

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        } else if (principal instanceof Jwt jwt) {
            return jwt.getSubject();
        } else if (principal instanceof String s) {
            return s;
        }
        return null;
    }

}

