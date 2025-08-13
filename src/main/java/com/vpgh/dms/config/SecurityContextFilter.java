package com.vpgh.dms.config;

import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.util.SecurityUtil;
import com.vpgh.dms.util.WhiteListUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
public class SecurityContextFilter extends OncePerRequestFilter {
    private final PathMatcher pathMatcher = new AntPathMatcher();

    private boolean isWhitelisted(String path) {
        String[] whiteList = WhiteListUtil.getPublicWhitelist();
        return Arrays.stream(whiteList)
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            if (!isWhitelisted(request.getRequestURI())) {
                User currentUser = SecurityUtil.getCurrentUser();
                if (currentUser != null) {
                    SecurityUtil.setCurrentUser(currentUser);
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            SecurityUtil.clear();
        }
    }

}

