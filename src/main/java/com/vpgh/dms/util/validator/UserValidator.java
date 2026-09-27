package com.vpgh.dms.util.validator;

import com.vpgh.dms.model.dto.UserDTO;
import com.vpgh.dms.service.RoleService;
import com.vpgh.dms.service.UserService;
import com.vpgh.dms.util.annotation.ValidUser;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
public class UserValidator implements ConstraintValidator<ValidUser, UserDTO> {
    private final UserService userService;
    private final RoleService roleService;
    private final MessageSource messageSource;

    public UserValidator(UserService userService, RoleService roleService, MessageSource messageSource) {
        this.userService = userService;
        this.roleService = roleService;
        this.messageSource = messageSource;
    }

    @Override
    public boolean isValid(UserDTO user, ConstraintValidatorContext context) {
        boolean valid = true;

        context.disableDefaultConstraintViolation();

        if (user.getRole() == null || user.getRole().getId() == null) {
            ValidationMessages.reject(context, messageSource, "role", "validation.user.role.notBlank");
            valid = false;
        } else {
            boolean existRole = this.roleService.existsById(user.getRole().getId());
            if (!existRole) {
                ValidationMessages.reject(context, messageSource, "role", "validation.user.role.invalid");
                valid = false;
            }
        }

        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            boolean existEmail = this.userService.existsByEmailAndIdNot(user.getEmail(), user.getId());
            if (existEmail) {
                ValidationMessages.reject(context, messageSource, "email", "validation.user.email.unique");
                valid = false;
            }
        }

        return valid;
    }
}
