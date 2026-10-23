package com.vpgh.dms.util.validator;

import com.vpgh.dms.model.dto.RoleDTO;
import com.vpgh.dms.model.entity.Permission;
import com.vpgh.dms.service.PermissionService;
import com.vpgh.dms.service.RoleService;
import com.vpgh.dms.util.annotation.ValidRole;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
public class RoleValidator implements ConstraintValidator<ValidRole, RoleDTO> {
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final MessageSource messageSource;

    public RoleValidator(RoleService roleService, PermissionService permissionService, MessageSource messageSource) {
        this.roleService = roleService;
        this.permissionService = permissionService;
        this.messageSource = messageSource;
    }

    @Override
    public boolean isValid(RoleDTO role, ConstraintValidatorContext context) {
        boolean valid = true;

        context.disableDefaultConstraintViolation();

        if (role.getName() != null && !role.getName().trim().isEmpty()) {
            boolean existName = this.roleService.existsByNameAndIdNot(role.getName(), role.getId());
            if (existName) {
                ValidationMessages.reject(context, messageSource, "name", "validation.role.name.unique");
                valid = false;
            }
        }

        if (role.getPermissions() != null) {
            for (Permission p : role.getPermissions()) {
                if (this.permissionService.getPermissionById(p.getId()) == null) {
                    ValidationMessages.reject(context, messageSource, "permissions",
                            "validation.role.permission.notFound", p.getId());
                    valid = false;
                    break;
                }
            }
        }

        return valid;
    }
}

