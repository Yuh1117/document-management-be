package com.vpgh.dms.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public class ProfileUpdateReq {
    @NotBlank(message = "{validation.user.firstName.notBlank}")
    @Size(max = 100, message = "{validation.profile.name.size}")
    private String firstName;

    @NotBlank(message = "{validation.user.lastName.notBlank}")
    @Size(max = 100, message = "{validation.profile.name.size}")
    private String lastName;

    /** Optional new avatar image. */
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

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}
