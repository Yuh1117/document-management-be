package com.vpgh.dms.model;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class TimestampEntityListener {

    @PrePersist
    public void prePersist(Object entity) {
        if (entity instanceof TimestampedEntity e) {
            e.setCreatedAt(Instant.now());
        }
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        if (entity instanceof TimestampedEntity e) {
            e.setUpdatedAt(Instant.now());
        }
    }
}

