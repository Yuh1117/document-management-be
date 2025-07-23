package com.vpgh.dms.util.annotation;

import com.vpgh.dms.util.validator.NotFoundFieldValidator;
import com.vpgh.dms.util.validator.UniqueFieldValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotFoundFieldValidator.class)
@Documented
public @interface NotFound {
    String message() default "Không tìm thấy {field}!";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    Class<?> entity();
    String field();
}
