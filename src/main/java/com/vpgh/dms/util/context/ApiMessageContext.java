package com.vpgh.dms.util.context;

import com.vpgh.dms.util.annotation.ApiMessage;

public class ApiMessageContext {
    private static final ThreadLocal<ApiMessage> context = new ThreadLocal<>();

    public static void set(ApiMessage message) {
        context.set(message);
    }

    public static ApiMessage get() {
        return context.get();
    }

    public static void clear() {
        context.remove();
    }
}

