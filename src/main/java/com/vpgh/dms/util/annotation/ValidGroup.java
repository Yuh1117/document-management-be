package com.vpgh.dms.util.annotation;

import com.vpgh.dms.util.validator.GroupValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = GroupValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidGroup {
    String message() default "{validation.payload.invalid}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}