package com.vpgh.dms.model;

import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.util.SecurityUtil;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class FullAuditableEntityListener {

    @PrePersist
    public void prePersist(Object entity) {
        if (entity instanceof FullAuditableEntity e) {
            e.setCreatedAt(Instant.now());

            User user = SecurityUtil.getCurrentUserFromThreadLocal();
            if (user != null) e.setCreatedBy(user);
        }
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        if (entity instanceof FullAuditableEntity e) {
            e.setUpdatedAt(Instant.now());

            User user = SecurityUtil.getCurrentUserFromThreadLocal();
            if (user != null) e.setUpdatedBy(user);
        }
    }
}

