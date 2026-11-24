package com.vpgh.dms.model.dto.response;

import java.util.UUID;

/** Assistant answers one user received from one model, summed from chat_messages.usage. */
public interface AiUsageUserProjection {
    UUID getUserId();

    String getEmail();

    String getName();

    String getModel();

    Long getAnswers();

    Long getInputTokens();

    Long getCachedTokens();

    Long getOutputTokens();
}
