package com.vpgh.dms.util.validator;

import com.vpgh.dms.repository.NotFoundCheckRepository;
import com.vpgh.dms.util.annotation.NotFound;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotFoundFieldValidator implements ConstraintValidator<NotFound, Object> {

    @Autowired
    private NotFoundCheckRepository notFoundCheckRepository;

    private Class<?> entityClass;
    private String fieldName;

    @Override
    public void initialize(NotFound constraintAnnotation) {
        this.entityClass = constraintAnnotation.entity();
        this.fieldName = constraintAnnotation.field();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        return this.notFoundCheckRepository.isFound(entityClass, fieldName, value);
    }
}
