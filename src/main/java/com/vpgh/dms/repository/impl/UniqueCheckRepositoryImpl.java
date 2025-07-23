package com.vpgh.dms.repository.impl;

import com.vpgh.dms.repository.UniqueCheckRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class UniqueCheckRepositoryImpl implements UniqueCheckRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public boolean isUnique(Class<?> entityClass, String fieldName, Object value) {
        if (value == null) return true;

        String jpql = String.format("SELECT COUNT(e) FROM %s e WHERE e.%s = :value",
                entityClass.getSimpleName(), fieldName);
        Long count = em.createQuery(jpql, Long.class).setParameter("value", value).getSingleResult();
        return count == 0;
    }
}

