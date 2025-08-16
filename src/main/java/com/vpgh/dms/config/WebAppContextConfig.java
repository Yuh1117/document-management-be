package com.vpgh.dms.config;

import com.vpgh.dms.util.WhiteListUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.stream.Stream;

@Configuration
public class WebAppContextConfig implements WebMvcConfigurer {
    @Autowired
    private ApiMessageInterceptor apiMessageInterceptor;
    @Autowired
    private PermissionInterceptor permissionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiMessageInterceptor);
//        registry.addInterceptor(permissionInterceptor)
//                .excludePathPatterns(Stream.concat(
//                        Stream.of(WhiteListUtil.getPublicWhitelist()),
//                        Stream.of(WhiteListUtil.getAuthenticatedWhitelist())).toArray(String[]::new));
    }
}
