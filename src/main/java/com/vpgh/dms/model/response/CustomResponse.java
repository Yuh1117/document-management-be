package com.vpgh.dms.model.response;

public class CustomResponse<T, K> {
    private int statusCode;
    private String message;
    private T data;
    private K error;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public K getError() {
        return error;
    }

    public void setError(K error) {
        this.error = error;
    }
}
