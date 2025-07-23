package com.vpgh.dms.util.annotation;

import com.vpgh.dms.util.validator.UniqueFieldValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueFieldValidator.class)
@Documented
public @interface Unique {
    String message() default "{field} đã tồn tại!";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    Class<?> entity();
    String field();
}

