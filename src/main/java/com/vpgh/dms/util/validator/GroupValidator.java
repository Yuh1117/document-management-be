package com.vpgh.dms.util.validator;

import com.vpgh.dms.model.UserGroupDTO;
import com.vpgh.dms.service.UserGroupService;
import com.vpgh.dms.util.SecurityUtil;
import com.vpgh.dms.util.annotation.ValidGroup;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GroupValidator implements ConstraintValidator<ValidGroup, UserGroupDTO> {
    @Autowired
    private UserGroupService userGroupService;

    @Override
    public boolean isValid(UserGroupDTO groupDTO, ConstraintValidatorContext context) {
        boolean valid = true;

        context.disableDefaultConstraintViolation();

        boolean check = this.userGroupService.existsByNameAndCreatedByAndIdNot(groupDTO.getName(),
                SecurityUtil.getCurrentUserFromThreadLocal(), groupDTO.getId());
        if (check) {
            context.buildConstraintViolationWithTemplate("Tên đã tồn tại!")
                    .addPropertyNode("name")
                    .addConstraintViolation();
            valid = false;
        }

        return valid;
    }
}
