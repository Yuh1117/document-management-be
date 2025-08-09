package com.vpgh.dms.model.dto.response;

import com.vpgh.dms.model.dto.UserDTO;

public class UserLoginResDTO {
    private String accessToken;
    private UserDTO user;

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
