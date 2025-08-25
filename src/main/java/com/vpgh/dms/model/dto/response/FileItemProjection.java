package com.vpgh.dms.model.dto.response;

import java.time.Instant;

public interface FileItemProjection {
    Integer getId();

    String getName();

    String getDescription();

    String getType();

    String getMimeType();

    Instant getCreatedAt();

    Instant getUpdatedAt();

    Integer getCreatedById();

    String getCreatedByEmail();

    Boolean getIsDeleted();

    String getPermission();

}
