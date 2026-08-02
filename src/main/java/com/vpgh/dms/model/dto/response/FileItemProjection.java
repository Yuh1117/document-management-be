package com.vpgh.dms.model.dto.response;

import java.time.Instant;
import java.util.UUID;

public interface FileItemProjection {
    UUID getId();

    String getName();

    String getDescription();

    String getType();

    String getMimeType();

    String getProcessingStatus();

    Instant getCreatedAt();

    Instant getUpdatedAt();

    UUID getCreatedById();

    String getCreatedByEmail();

    String getCreatedByFirstName();

    String getCreatedByLastName();


    Boolean getIsDeleted();

    String getPermission();

}
