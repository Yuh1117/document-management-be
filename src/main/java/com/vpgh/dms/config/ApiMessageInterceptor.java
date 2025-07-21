package com.vpgh.dms.config;

import com.vpgh.dms.util.annotation.ApiMessage;
import com.vpgh.dms.util.context.ApiMessageContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiMessageInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (handler instanceof HandlerMethod handlerMethod) {
            ApiMessage apiMessage = handlerMethod.getMethodAnnotation(ApiMessage.class);
            ApiMessageContext.set(apiMessage);
        }
        return true;
    }
}

