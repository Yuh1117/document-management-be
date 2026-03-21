package com.vpgh.dms.util.annotation;

import com.vpgh.dms.util.validator.RoleValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = RoleValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRole {
    String message() default "{validation.payload.invalid}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
