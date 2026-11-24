package com.vpgh.dms.model.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @param conversationId existing conversation to append to; null starts a new one.
 */
public record RagAskReq(
        @NotBlank @Size(max = 500) String question,
        UUID conversationId
) {
}
