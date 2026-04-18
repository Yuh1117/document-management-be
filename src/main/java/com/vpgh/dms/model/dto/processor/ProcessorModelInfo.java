package com.vpgh.dms.model.dto.processor;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProcessorModelInfo(
        String version,
        @JsonProperty("model_name") String modelName,
        @JsonProperty("is_active") boolean isActive,
        @JsonProperty("created_at") String createdAt
) {
}
