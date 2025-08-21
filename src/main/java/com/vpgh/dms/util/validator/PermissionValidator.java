package com.vpgh.dms.util.validator;

import com.vpgh.dms.model.dto.PermissionDTO;
import com.vpgh.dms.service.PermissionService;
import com.vpgh.dms.util.annotation.ValidPermission;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PermissionValidator implements ConstraintValidator<ValidPermission, PermissionDTO> {

    private final PermissionService permissionService;

    public PermissionValidator(PermissionService permissionService) {
        this.permissionService = permissionService;
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
                context.buildConstraintViolationWithTemplate("Quyền đã tồn tại!")
                        .addPropertyNode("apiPath")
                        .addConstraintViolation();
                context.buildConstraintViolationWithTemplate("Quyền đã tồn tại!")
                        .addPropertyNode("method")
                        .addConstraintViolation();
                valid = false;
            }
        }

        return valid;
    }
}
