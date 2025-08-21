package com.vpgh.dms.util;

import com.vpgh.dms.util.annotation.ApiMessage;
import com.vpgh.dms.util.context.ApiMessageContext;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@RestControllerAdvice
public class FormatCustomResponse implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        Class<?> paramType = returnType.getParameterType();

        if (paramType == byte[].class || Resource.class.isAssignableFrom(paramType)) {
            return false;
        }

        Type type = returnType.getGenericParameterType();
        if (type instanceof ParameterizedType parameterizedType) {
            Type rawType = parameterizedType.getRawType();
            if (rawType == ResponseEntity.class) {
                Type actualType = parameterizedType.getActualTypeArguments()[0];
                if (actualType == byte[].class || actualType instanceof Class &&
                        Resource.class.isAssignableFrom((Class<?>) actualType)) {
                    return false;
                }
            }
        }

        return true;
    }


    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        HttpServletResponse servletResponse = ((ServletServerHttpResponse) response).getServletResponse();
        int status = servletResponse.getStatus();

        CustomResponse<Object, Object> res = new CustomResponse<>();
        res.setStatusCode(status);

        ApiMessage apiMessage = returnType.getMethodAnnotation(ApiMessage.class);
        if (apiMessage == null) {
            apiMessage = ApiMessageContext.get();
        }

        if (status < 400) {
            res.setMessage(ApiMessageUtil.getSuccessMessage(apiMessage));
            if (body instanceof DataResponse<?> data) {
                res.setData(data.getContent());
            } else {
                res.setData(body);
            }
        } else {
            if (status == 500) {
                res.setMessage("Internal server error");
            } else {
                res.setMessage(ApiMessageUtil.getFailedMessage(apiMessage));
            }
            if (body instanceof DataResponse<?> err) {
                res.setError(err.getContent());
            } else {
                res.setError(body);
            }
        }
        ApiMessageContext.clear();
        return res;
    }
}
