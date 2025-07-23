package com.vpgh.dms.repository;

public interface NotFoundCheckRepository {
    boolean isFound(Class<?> entityClass, String fieldName, Object value);
}
