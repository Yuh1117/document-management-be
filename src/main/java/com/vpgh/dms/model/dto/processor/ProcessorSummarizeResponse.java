package com.vpgh.dms.model.dto.processor;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProcessorSummarizeResponse(
        @JsonProperty("summary_text") String summaryText,
        @JsonProperty("model_name") String modelName,
        @JsonProperty("prompt_version") String promptVersion
) {
}
