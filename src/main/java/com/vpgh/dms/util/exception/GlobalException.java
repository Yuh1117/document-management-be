package com.vpgh.dms.util.exception;

import com.vpgh.dms.util.CustomResponse;
import com.vpgh.dms.util.DataResponse;
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
            NotFoundException.class})
    public ResponseEntity<DataResponse<String>> handleException(Exception ex) {
        DataResponse<String> errorResponse = new DataResponse<>();
        errorResponse.setContent(ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<DataResponse<List<Map<String, String>>>> handleException(MethodArgumentNotValidException ex) {
        DataResponse<List<Map<String, String>>> errorResponse = new DataResponse<>();

        List<Map<String, String>> errors = new ArrayList<>();
        if (ex.getBindingResult().hasErrors()) {
            errors = ex.getBindingResult().getFieldErrors().stream().map(error -> {
                Map<String, String> err = new HashMap<>();
                err.put("field", error.getField());
                err.put("message", error.getDefaultMessage());
                return err;
            }).collect(Collectors.toList());
        }
        errorResponse.setContent(errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(CustomValidationException.class)
    public ResponseEntity<DataResponse<List<Map<String, String>>>> handleException(CustomValidationException ex) {
        DataResponse<List<Map<String, String>>> errorResponse = new DataResponse<>();
        errorResponse.setContent(ex.getErrors());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(value = BadCredentialsException.class)
    public ResponseEntity<DataResponse<String>> handleException(BadCredentialsException ex) {
        DataResponse<String> errorResponse = new DataResponse<>();
        errorResponse.setContent(ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(value = DataConflictException.class)
    public ResponseEntity<DataResponse<String>> handleException(DataConflictException ex) {
        DataResponse<String> errorResponse = new DataResponse<>();
        errorResponse.setContent(ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<DataResponse<String>> handleException(ForbiddenException ex) {
        DataResponse<String> errorResponse = new DataResponse<>();
        errorResponse.setContent(ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<DataResponse<Object>> handleAllException(Exception ex) {
        DataResponse<Object> errorResponse = new DataResponse<>();
        errorResponse.setContent(ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

}
