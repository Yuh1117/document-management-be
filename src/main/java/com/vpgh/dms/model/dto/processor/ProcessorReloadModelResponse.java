package com.vpgh.dms.model.dto.processor;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProcessorReloadModelResponse(
        @JsonProperty("previous_model") String previousModel,
        @JsonProperty("current_model") String currentModel,
        @JsonProperty("mlflow_model_uri") String mlflowModelUri,
        @JsonProperty("status") String status
) {
}
