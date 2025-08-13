package com.vpgh.dms.util;

public class WhiteListUtil {

    private static final String[] PUBLIC_WHITELIST = {
            "/api/login", "/api/signup", "/api/auth/google",
            "/dms-api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
    };

    private static final String[] AUTHENTICATED_WHITELIST = {
            "/api/secure/profile", "/api/secure/check-permissions"
    };

    public static String[] getPublicWhitelist() {
        return PUBLIC_WHITELIST;
    }

    public static String[] getAuthenticatedWhitelist() {
        return AUTHENTICATED_WHITELIST;
    }

}
