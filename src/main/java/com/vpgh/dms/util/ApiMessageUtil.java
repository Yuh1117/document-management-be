package com.vpgh.dms.util;

import com.vpgh.dms.util.annotation.ApiMessage;

public class ApiMessageUtil {
    private static final String SUCCESS = "thành công";
    private static final String FAILURE = "thất bại";

    public static String getSuccessMessage(ApiMessage apiMessage) {
        if (apiMessage == null || apiMessage.message() == null) {
            return "hehe";
        }
        return apiMessage.message() + " " + SUCCESS;
    }

    public static String getFailedMessage(ApiMessage apiMessage) {
        if (apiMessage == null || apiMessage.message() == null) {
            return "hehe";
        }
        return apiMessage.message() + " " + FAILURE;
    }
}

