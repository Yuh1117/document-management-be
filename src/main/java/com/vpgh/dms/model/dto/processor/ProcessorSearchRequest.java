package com.vpgh.dms.model.dto.processor;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ProcessorSearchRequest(
        String query,
        @JsonProperty("owner_id") UUID ownerId,
        @JsonProperty("folder_id") UUID folderId,
        int page,
        @JsonProperty("page_size") int pageSize,
        String mode
) {
}
