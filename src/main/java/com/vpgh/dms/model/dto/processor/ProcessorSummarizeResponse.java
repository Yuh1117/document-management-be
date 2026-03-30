package com.vpgh.dms.model.dto.processor;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProcessorSummarizeResponse(
        @JsonProperty("summary_text") String summaryText,
        @JsonProperty("model_version") String modelVersion,
        @JsonProperty("prompt_version") String promptVersion
) {
}
