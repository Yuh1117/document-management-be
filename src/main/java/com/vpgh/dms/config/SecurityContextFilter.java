package com.vpgh.dms.config;

import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.util.SecurityUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            User currentUser = SecurityUtil.getCurrentUser();
            if (currentUser != null) {
                SecurityUtil.setCurrentUser(currentUser);
            }

            filterChain.doFilter(request, response);
        } finally {
            SecurityUtil.clear();
        }
    }
}

