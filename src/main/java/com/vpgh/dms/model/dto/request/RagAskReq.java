package com.vpgh.dms.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RagAskReq(
        @NotBlank @Size(max = 500) String question
) {
}
