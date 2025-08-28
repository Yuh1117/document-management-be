package com.vpgh.dms.util;

public class DataResponse<T> {
    private T content;

    public DataResponse() {

    }

    public DataResponse(T content) {
        this.content = content;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }
}
