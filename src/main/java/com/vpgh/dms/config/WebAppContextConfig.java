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
//        registry.addInterceptor(permissionInterceptor).excludePathPatterns("/api/login");
    }
}
