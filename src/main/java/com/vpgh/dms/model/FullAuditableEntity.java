package com.vpgh.dms.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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

    @ManyToOne
    @JoinColumn(name = "created_by")
    @JsonSerialize(using = UserDTOSerializer.class)
    private User createdBy;
    @ManyToOne
    @JoinColumn(name = "updated_by")
    @JsonSerialize(using = UserDTOSerializer.class)
    private User updatedBy;

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }

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

