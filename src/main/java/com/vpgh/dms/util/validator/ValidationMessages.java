package com.vpgh.dms.util.validator;

import jakarta.validation.ConstraintValidatorContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

public final class ValidationMessages {

    private ValidationMessages() {
    }

    public static void reject(ConstraintValidatorContext context, MessageSource messageSource,
                               String property, String messageCode, Object... args) {
        String message = messageSource.getMessage(messageCode, args, LocaleContextHolder.getLocale());
        // Escape literal braces/backslashes so Bean Validation's default interpolator
        // doesn't try to re-interpret them (e.g. a filename containing "{}" or a message key).
        String escaped = message.replace("\\", "\\\\").replace("{", "\\{").replace("}", "\\}");
        context.buildConstraintViolationWithTemplate(escaped)
                .addPropertyNode(property)
                .addConstraintViolation();
    }
}
