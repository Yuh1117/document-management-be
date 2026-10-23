package com.vpgh.dms.util.validator;

import com.vpgh.dms.model.dto.PermissionDTO;
import com.vpgh.dms.service.PermissionService;
import com.vpgh.dms.util.annotation.ValidPermission;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.context.MessageSource;

public class PermissionValidator implements ConstraintValidator<ValidPermission, PermissionDTO> {

    private final PermissionService permissionService;
    private final MessageSource messageSource;

    public PermissionValidator(PermissionService permissionService, MessageSource messageSource) {
        this.permissionService = permissionService;
        this.messageSource = messageSource;
    }

    @Override
    public boolean isValid(PermissionDTO permission, ConstraintValidatorContext context) {
        boolean valid = true;

        context.disableDefaultConstraintViolation();

        if ((permission.getApiPath() != null && !permission.getApiPath().trim().isEmpty()) &&
                permission.getMethod() != null && !permission.getMethod().trim().isEmpty() &&
                permission.getModule() != null && !permission.getModule().trim().isEmpty()) {
            boolean exist = this.permissionService.existsByApiPathAndMethodAndIdNot(permission.getApiPath(),
                    permission.getMethod(), permission.getId());
            if (exist) {
                ValidationMessages.reject(context, messageSource, "apiPath", "validation.permission.unique");
                ValidationMessages.reject(context, messageSource, "method", "validation.permission.unique");
                valid = false;
            }
        }

        return valid;
    }
}
