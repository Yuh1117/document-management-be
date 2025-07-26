package com.vpgh.dms.util.validator;

import com.vpgh.dms.model.dto.RoleDTO;
import com.vpgh.dms.model.entity.Permission;
import com.vpgh.dms.service.PermissionService;
import com.vpgh.dms.service.RoleService;
import com.vpgh.dms.util.annotation.ValidRole;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RoleValidator implements ConstraintValidator<ValidRole, RoleDTO> {

    @Autowired
    private RoleService roleService;

    @Autowired
    private PermissionService permissionService;

    @Override
    public boolean isValid(RoleDTO role, ConstraintValidatorContext context) {
        boolean valid = true;

        context.disableDefaultConstraintViolation();

        boolean existName = this.roleService.existsByNameAndIdNot(role.getName(), role.getId());
        if (existName) {
            context.buildConstraintViolationWithTemplate("Tên đã tồn tại!")
                    .addPropertyNode("name")
                    .addConstraintViolation();
            valid = false;
        }

        if (role.getPermissions() != null) {
            for (Permission p : role.getPermissions()) {
                if (this.permissionService.getPermissionById(p.getId()) == null) {
                    context.buildConstraintViolationWithTemplate("Không có quyền nào với id " + p.getId())
                            .addPropertyNode("permissions")
                            .addConstraintViolation();
                    valid = false;
                    break;
                }
            }
        }

        return valid;
    }
}

