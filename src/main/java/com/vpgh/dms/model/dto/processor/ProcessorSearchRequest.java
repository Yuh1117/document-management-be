package com.vpgh.dms.model.dto.processor;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ProcessorSearchRequest(
        String query,
        @JsonProperty("owner_id") UUID ownerId,
        int page,
        @JsonProperty("page_size") int pageSize,
        String mode,
        @JsonProperty("mime_pattern") String mimePattern,
        @JsonProperty("size_min") Long sizeMin,
        @JsonProperty("size_max") Long sizeMax
) {
}
