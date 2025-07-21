package com.vpgh.dms.util;

public class ErrorResponse<T> {
    private T error;

    public T getError() {
        return error;
    }

    public void setError(T error) {
        this.error = error;
    }
}
