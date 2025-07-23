package com.vpgh.dms.model.dto.response;

public class UserLoginResDTO {
    private UserResDTO user;
    private String accessToken;

    public UserResDTO getUser() {
        return user;
    }

    public void setUser(UserResDTO user) {
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
