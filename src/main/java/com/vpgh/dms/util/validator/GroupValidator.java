package com.vpgh.dms.util.validator;

import com.vpgh.dms.model.dto.UserGroupDTO;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.model.entity.UserGroup;
import com.vpgh.dms.service.UserGroupService;
import com.vpgh.dms.service.UserService;
import com.vpgh.dms.util.SecurityUtil;
import com.vpgh.dms.util.annotation.ValidGroup;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GroupValidator implements ConstraintValidator<ValidGroup, UserGroupDTO> {
    private final UserGroupService userGroupService;
    private final UserService userService;
    private final MessageSource messageSource;

    public GroupValidator(UserGroupService userGroupService, UserService userService, MessageSource messageSource) {
        this.userGroupService = userGroupService;
        this.userService = userService;
        this.messageSource = messageSource;
    }

    private static final ThreadLocal<UserGroup> currentEntity = new ThreadLocal<>();

    public static void setCurrentEntity(UserGroup group) {
        currentEntity.set(group);
    }

    @Override
    public boolean isValid(UserGroupDTO groupDTO, ConstraintValidatorContext context) {
        boolean valid = true;

        context.disableDefaultConstraintViolation();

        boolean check = false;
        if (groupDTO.getId() != null) {
            check = this.userGroupService.existsByNameAndCreatedByAndIdNot(groupDTO.getName(),
                    currentEntity.get().getCreatedBy(), groupDTO.getId());
        } else {
            check = this.userGroupService.existsByNameAndCreatedByAndIdNot(groupDTO.getName(),
                    SecurityUtil.getCurrentUserFromThreadLocal(), groupDTO.getId());
        }
        if (check) {
            ValidationMessages.reject(context, messageSource, "name", "validation.group.name.unique");
            valid = false;
        }

        List<UserGroupDTO.MemberDTO> members = groupDTO.getMembers();
        if (members != null) {
            for (int i = 0; i < members.size(); i++) {
                String email = members.get(i).getEmail();
                if (email == null || email.isEmpty()) {
                    ValidationMessages.reject(context, messageSource, "member " + (i + 1), "validation.email.notBlank");
                    valid = false;
                } else if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                    ValidationMessages.reject(context, messageSource, "member " + (i + 1), "validation.email.invalid");
                    valid = false;
                } else {
                    User user = this.userService.getUserByEmail(members.get(i).getEmail());
                    if (user == null) {
                        ValidationMessages.reject(context, messageSource, "email", "validation.email.userNotFound",
                                members.get(i).getEmail());
                        valid = false;
                    }
                }
            }
        }

        currentEntity.remove();
        return valid;
    }
}
