package com.vpgh.dms.model.dto.response;

import com.vpgh.dms.model.dto.UserDTO;

import java.time.Instant;

public interface FileItemProjection {
    Integer getId();

    String getName();

    String getType();

    Instant getCreatedAt();

    Instant getUpdatedAt();

    UserDTO getCreatedBy();

    Integer getCreatedById();

    String getCreatedByEmail();
}
