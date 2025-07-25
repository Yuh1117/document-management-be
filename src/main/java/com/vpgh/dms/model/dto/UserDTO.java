package com.vpgh.dms.model.dto;

import com.vpgh.dms.model.entity.Role;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.util.annotation.NotFound;
import com.vpgh.dms.util.annotation.Unique;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public class UserDTO {
    public interface CreateGroup {
    }

    public interface UpdateGroup {
    }

    @Email(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Email không hợp lệ!",
            groups = {CreateGroup.class, UpdateGroup.class})
    @NotBlank(message = "Email không được để trống", groups = {CreateGroup.class, UpdateGroup.class})
    @Unique(entity = User.class, field = "email", message = "Email đã tồn tại", groups = {CreateGroup.class})
    private String email;
    @NotBlank(message = "Mật khẩu không được để trống", groups = {CreateGroup.class, UpdateGroup.class})
    private String password;
    @NotBlank(message = "Tên không được để trống", groups = {CreateGroup.class, UpdateGroup.class})
    private String firstName;
    @NotBlank(message = "Họ không được để trống", groups = {CreateGroup.class, UpdateGroup.class})
    private String lastName;
    private String avatar;
    @NotNull(message = "Role không được để trống", groups = {CreateGroup.class, UpdateGroup.class})
    @NotFound(entity = Role.class, field = "id", message = "Không tìm thấy role", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer roleId;
    private MultipartFile file;

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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}
