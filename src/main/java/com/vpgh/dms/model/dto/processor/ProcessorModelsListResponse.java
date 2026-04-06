package com.vpgh.dms.model.dto.processor;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ProcessorModelsListResponse(
        @JsonProperty("models") List<ProcessorModelInfo> models
) {
}
