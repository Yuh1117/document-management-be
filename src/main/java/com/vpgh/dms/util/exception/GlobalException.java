package com.vpgh.dms.util.exception;

import com.vpgh.dms.model.dto.response.CustomResponse;
import com.vpgh.dms.util.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalException {
    @ExceptionHandler(value = {
            UniqueConstraintException.class,
            UsernameNotFoundException.class,
            IdInvalidException.class})
    public ResponseEntity<ErrorResponse<String>> handleException(Exception ex) {
        ErrorResponse<String> errorResponse = new ErrorResponse<>();
        errorResponse.setError(ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse<List<Map<String, String>>>> handleException(MethodArgumentNotValidException ex) {
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

    @ExceptionHandler(CustomValidationException.class)
    public ResponseEntity<ErrorResponse<List<Map<String, String>>>> handleException(CustomValidationException ex) {
        ErrorResponse<List<Map<String, String>>> errorResponse = new ErrorResponse<>();
        errorResponse.setError(ex.getErrors());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(value = BadCredentialsException.class)
    public ResponseEntity<ErrorResponse<String>> handleException(BadCredentialsException ex) {
        ErrorResponse<String> errorResponse = new ErrorResponse<>();
        errorResponse.setError(ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<CustomResponse<Object, Object>> handleAllException(Exception ex) {
        CustomResponse<Object, Object> customResponse = new CustomResponse<>();
        customResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        customResponse.setMessage("Internal server error");
        customResponse.setError(ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(customResponse);
    }

}
