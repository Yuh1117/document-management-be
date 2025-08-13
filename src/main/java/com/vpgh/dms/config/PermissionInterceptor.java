package com.vpgh.dms.config;

import com.vpgh.dms.model.entity.Permission;
import com.vpgh.dms.model.entity.Role;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.service.PermissionService;
import com.vpgh.dms.util.SecurityUtil;
import com.vpgh.dms.util.exception.ForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.HashSet;
import java.util.Set;

@Component
public class PermissionInterceptor implements HandlerInterceptor {
    @Autowired
    private PermissionService permissionService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String requestURI = request.getRequestURI();
        String httpMethod = request.getMethod();

        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();
        if (currentUser != null) {
            Role role = currentUser.getRole();
            if (role != null) {
                Set<Permission> permissions = new HashSet<>(this.permissionService.getPermissionsByRole(role));
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

