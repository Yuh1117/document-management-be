package com.vpgh.dms.util.annotation;

import com.vpgh.dms.util.validator.FileValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FileValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFile {
    String message() default "{validation.payload.invalid}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
