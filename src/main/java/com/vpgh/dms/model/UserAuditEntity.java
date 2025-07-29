package com.vpgh.dms.model;

import com.vpgh.dms.model.entity.User;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
@EntityListeners(UserAuditEntityListener.class)
public abstract class UserAuditEntity {

    public abstract User getCreatedBy();

    public abstract void setCreatedBy(User createdBy);

    public abstract User getUpdatedBy();

    public abstract void setUpdatedBy(User updatedBy);
}

