package com.vpgh.dms.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebAppContextConfig implements WebMvcConfigurer {
    @Autowired
    private ApiMessageInterceptor apiMessageInterceptor;
    @Autowired
    private PermissionInterceptor permissionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiMessageInterceptor);

        String[] whiteList = {"/", "/api/login", "/api/signup", "/api/auth/google", "/api/secure/profile",
                "/dms-api-docs/**", "/swagger-ui/**", "/swagger-ui.html"};
        registry.addInterceptor(permissionInterceptor).excludePathPatterns(whiteList);
    }
}
