package com.vpgh.dms.model;

import com.vpgh.dms.model.entity.User;
import jakarta.persistence.*;

import java.time.Instant;

@MappedSuperclass
@EntityListeners(FullAuditableEntityListener.class)
public abstract class FullAuditableEntity {
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public abstract User getCreatedBy();

    public abstract void setCreatedBy(User createdBy);

    public abstract User getUpdatedBy();

    public abstract void setUpdatedBy(User updatedBy);

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

