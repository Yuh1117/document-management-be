package com.vpgh.dms.model.dto.processor;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProcessorSearchRequest(
        String query,
        @JsonProperty("owner_id") Integer ownerId,
        @JsonProperty("folder_id") Integer folderId,
        int page,
        @JsonProperty("page_size") int pageSize,
        String mode
) {
}
