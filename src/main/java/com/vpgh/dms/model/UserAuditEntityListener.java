package com.vpgh.dms.model;

import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.util.SecurityUtil;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.springframework.stereotype.Component;

@Component
public class UserAuditEntityListener {

    @PrePersist
    public void prePersist(Object entity) {
        if (entity instanceof UserAuditEntity e) {
            User user = SecurityUtil.getCurrentUser();
            if (user != null) e.setCreatedBy(user);
        }
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        if (entity instanceof UserAuditEntity e) {
            User user = SecurityUtil.getCurrentUser();
            if (user != null) e.setUpdatedBy(user);
        }
    }
}

