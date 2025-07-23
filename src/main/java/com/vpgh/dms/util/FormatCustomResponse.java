package com.vpgh.dms.util;

import com.vpgh.dms.model.dto.response.CustomResponse;
import com.vpgh.dms.util.annotation.ApiMessage;
import com.vpgh.dms.util.context.ApiMessageContext;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class FormatCustomResponse implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
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
            res.setData(body);
        } else {
            if (body instanceof ErrorResponse err) {
                res.setMessage(ApiMessageUtil.getFailedMessage(apiMessage));
                res.setError(err.getError());
            } else {
                return body;
            }
        }
        ApiMessageContext.clear();
        return res;
    }
}
