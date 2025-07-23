package com.vpgh.dms.repository;

public interface UniqueCheckRepository {
    boolean isUnique(Class<?> entityClass, String fieldName, Object value);
}
