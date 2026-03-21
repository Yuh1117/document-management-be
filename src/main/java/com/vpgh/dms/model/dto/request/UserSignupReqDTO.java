package com.vpgh.dms.model.dto.request;

import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.util.annotation.Unique;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

public class UserSignupReqDTO {
    @NotBlank(message = "{validation.user.firstName.notBlank}")
    private String firstName;
    @NotBlank(message = "{validation.user.lastName.notBlank}")
    private String lastName;
    @Email(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "{validation.user.email.invalid}")
    @NotBlank(message = "{validation.user.email.notBlank}")
    @Unique(entity = User.class, field = "email", message = "{validation.user.email.unique}")
    private String email;
    @NotBlank(message = "{validation.user.password.notBlank}")
    private String password;
    private MultipartFile file;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}
