package com.vpgh.dms.exception;

import com.vpgh.dms.util.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalException {
    @ExceptionHandler(value = {UniqueConstraintException.class, UsernameNotFoundException.class})
    public ResponseEntity<ErrorResponse<String>> handleException(UniqueConstraintException ex, WebRequest request) {
        ErrorResponse<String> errorResponse = new ErrorResponse<>();
        errorResponse.setError(ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse<List<Map<String, String>>>> handleException(MethodArgumentNotValidException ex,
                                                                                    WebRequest request) {
        ErrorResponse<List<Map<String, String>>> errorResponse = new ErrorResponse<>();

        List<Map<String, String>> errors = new ArrayList<>();
        if (ex.getBindingResult().hasErrors()) {
            errors = ex.getBindingResult().getFieldErrors().stream().map(error -> {
                Map<String, String> err = new HashMap<>();
                err.put("field", error.getField());
                err.put("message", error.getDefaultMessage());
                return err;
            }).collect(Collectors.toList());
        }
        errorResponse.setError(errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
