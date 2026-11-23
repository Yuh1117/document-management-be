package com.vpgh.dms.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordReq {
    @NotBlank(message = "{validation.profile.currentPassword.notBlank}")
    private String currentPassword;

    @NotBlank(message = "{validation.user.password.notBlank}")
    @Size(min = 6, max = 100, message = "{validation.profile.newPassword.size}")
    private String newPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
