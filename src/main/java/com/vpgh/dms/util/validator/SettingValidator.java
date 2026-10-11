package com.vpgh.dms.util.validator;

import com.vpgh.dms.model.dto.SystemSettingDTO;
import com.vpgh.dms.service.SystemSettingService;
import com.vpgh.dms.util.annotation.ValidSetting;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.context.MessageSource;

public class SettingValidator implements ConstraintValidator<ValidSetting, SystemSettingDTO> {
    private final SystemSettingService systemSettingService;
    private final MessageSource messageSource;

    public SettingValidator(SystemSettingService systemSettingService, MessageSource messageSource) {
        this.systemSettingService = systemSettingService;
        this.messageSource = messageSource;
    }

    @Override
    public boolean isValid(SystemSettingDTO setting, ConstraintValidatorContext context) {
        boolean valid = true;

        context.disableDefaultConstraintViolation();

        if (setting.getKey() != null && !setting.getKey().trim().isEmpty()) {
            boolean check = this.systemSettingService.existsByKeyAndIdNot(setting.getKey(), setting.getId());
            if (check) {
                ValidationMessages.reject(context, messageSource, "key", "validation.setting.key.unique");
                valid = false;
            }
        }

        return valid;
    }
}
