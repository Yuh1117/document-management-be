package com.vpgh.dms.model.dto;

import com.vpgh.dms.model.constant.MemberEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class MemberDTO {
    @Email(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Email không hợp lệ!")
    @NotBlank(message = "Email không được để trống")
    private String email;

    private MemberEnum role;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public MemberEnum getRole() {
        return role;
    }

    public void setRole(MemberEnum role) {
        this.role = role;
    }
}
