package com.vpgh.dms.model.dto.processor;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProcessorModelInfo(
        String id,
        String provider,
        @JsonProperty("is_default") boolean isDefault,
        @JsonProperty("prompt_version") String promptVersion
) {
}
