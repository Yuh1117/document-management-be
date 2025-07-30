package com.vpgh.dms.util.validator;

import com.vpgh.dms.model.dto.SystemSettingDTO;
import com.vpgh.dms.service.SystemSettingService;
import com.vpgh.dms.util.annotation.ValidSetting;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class SettingValidator implements ConstraintValidator<ValidSetting, SystemSettingDTO> {

    @Autowired
    private SystemSettingService systemSettingService;

    @Override
    public boolean isValid(SystemSettingDTO setting, ConstraintValidatorContext context) {
        boolean valid = true;

        context.disableDefaultConstraintViolation();

        boolean check = this.systemSettingService.existsByKeyAndIdNot(setting.getKey(), setting.getId());
        if (check) {
            context.buildConstraintViolationWithTemplate("Key đã tồn tại!")
                    .addPropertyNode("key")
                    .addConstraintViolation();
            valid = false;
        }

        return valid;
    }
}
