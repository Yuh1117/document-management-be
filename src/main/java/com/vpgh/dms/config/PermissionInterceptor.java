package com.vpgh.dms.config;

import com.vpgh.dms.model.entity.Permission;
import com.vpgh.dms.model.entity.Role;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.util.SecurityUtil;
import com.vpgh.dms.util.exception.ForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Set;

@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String requestURI = request.getRequestURI();
        String httpMethod = request.getMethod();

        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();
        if (currentUser != null) {
            Role role = currentUser.getRole();
            if (role != null) {
                Set<Permission> permissions = role.getPermissions();
                    boolean isAllow = permissions.stream().anyMatch(i -> i.getApiPath().equals(path) &&
                        i.getMethod().equals(httpMethod));

                if (!isAllow) {
                    throw new ForbiddenException("Bạn không có quyền thực hiện hành động này!");
                }
            } else {
                throw new ForbiddenException("Bạn không có quyền thực hiện hành động này!");
            }
        }

        return true;
    }
}

