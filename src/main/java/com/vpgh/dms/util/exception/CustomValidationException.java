package com.vpgh.dms.util.exception;

import java.util.List;
import java.util.Map;

public class CustomValidationException extends RuntimeException {

    private final List<Map<String, String>> errors;

    public CustomValidationException(List<Map<String, String>> errors) {
        this.errors = errors;
    }

    public List<Map<String, String>> getErrors() {
        return errors;
    }
}

