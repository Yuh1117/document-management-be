package com.vpgh.dms.repository.impl;

import com.vpgh.dms.repository.NotFoundCheckRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class NotFoundCheckRepositoryImpl implements NotFoundCheckRepository {
    @PersistenceContext
    private EntityManager em;

    @Override
    public boolean isFound(Class<?> entityClass, String fieldName, Object value) {
        if (value == null) return true;

        String jpql = String.format("SELECT e FROM %s e WHERE e.%s = :value",
                entityClass.getSimpleName(), fieldName);

        List<?> result = em.createQuery(jpql, entityClass)
                .setParameter("value", value).setMaxResults(1).getResultList();

        return !result.isEmpty();
    }
}
