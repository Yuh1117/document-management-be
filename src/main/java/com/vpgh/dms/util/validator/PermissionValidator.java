package com.vpgh.dms.util.validator;

import com.vpgh.dms.model.dto.PermissionDTO;
import com.vpgh.dms.service.PermissionService;
import com.vpgh.dms.util.annotation.ValidPermission;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class PermissionValidator implements ConstraintValidator<ValidPermission, PermissionDTO> {

    @Autowired
    private PermissionService permissionService;

    @Override
    public boolean isValid(PermissionDTO permission, ConstraintValidatorContext context) {
        boolean valid = true;

        context.disableDefaultConstraintViolation();

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

        return valid;
    }
}
