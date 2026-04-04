package com.vpgh.dms.model.dto.processor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProcessorSummarizeRequest(
        String text,
        String language,
        @JsonProperty("model_id") String modelId
) {
    public ProcessorSummarizeRequest(String text, String language) {
        this(text, language, null);
    }
}
