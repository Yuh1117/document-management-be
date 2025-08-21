package com.vpgh.dms.util.validator;

import com.vpgh.dms.repository.UniqueCheckRepository;
import com.vpgh.dms.util.annotation.Unique;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class UniqueFieldValidator implements ConstraintValidator<Unique, Object> {
    private final UniqueCheckRepository uniqueCheckRepository;

    public UniqueFieldValidator(UniqueCheckRepository uniqueCheckRepository) {
        this.uniqueCheckRepository = uniqueCheckRepository;
    }

    private Class<?> entityClass;
    private String fieldName;

    @Override
    public void initialize(Unique constraintAnnotation) {
        this.entityClass = constraintAnnotation.entity();
        this.fieldName = constraintAnnotation.field();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        return this.uniqueCheckRepository.isUnique(entityClass, fieldName, value);
    }
}

