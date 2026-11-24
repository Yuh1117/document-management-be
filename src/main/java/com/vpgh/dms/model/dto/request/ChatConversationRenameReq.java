package com.vpgh.dms.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatConversationRenameReq(
        @NotBlank @Size(max = 255) String title
) {
}
