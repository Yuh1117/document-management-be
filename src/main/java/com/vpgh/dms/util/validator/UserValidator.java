package com.vpgh.dms.util.validator;

import com.vpgh.dms.model.dto.UserDTO;
import com.vpgh.dms.service.RoleService;
import com.vpgh.dms.service.UserService;
import com.vpgh.dms.util.annotation.ValidUser;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class UserValidator implements ConstraintValidator<ValidUser, UserDTO> {
    private final UserService userService;
    private final RoleService roleService;

    public UserValidator(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @Override
    public boolean isValid(UserDTO user, ConstraintValidatorContext context) {
        boolean valid = true;

        context.disableDefaultConstraintViolation();

        if (user.getRole() == null || user.getRole().getId() == null) {
            context.buildConstraintViolationWithTemplate("Vai trò không được để trống!")
                    .addPropertyNode("role")
                    .addConstraintViolation();
            valid = false;
        } else {
            boolean existRole = this.roleService.existsById(user.getRole().getId());
            if (!existRole) {
                context.buildConstraintViolationWithTemplate("Vai trò không hợp lệ!")
                        .addPropertyNode("role")
                        .addConstraintViolation();
                valid = false;
            }
        }

        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            boolean existEmail = this.userService.existsByEmailAndIdNot(user.getEmail(), user.getId());
            if (existEmail) {
                context.buildConstraintViolationWithTemplate("Email đã tồn tại!")
                        .addPropertyNode("email")
                        .addConstraintViolation();
                valid = false;
            }
        }

        return valid;
    }
}