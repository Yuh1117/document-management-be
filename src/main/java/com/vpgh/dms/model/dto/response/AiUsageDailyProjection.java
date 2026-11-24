package com.vpgh.dms.model.dto.response;

/** Assistant answers of one model on one day, summed from chat_messages.usage. */
public interface AiUsageDailyProjection {
    String getDay();

    String getModel();

    Long getAnswers();

    Long getInputTokens();

    Long getCachedTokens();

    Long getOutputTokens();

    Long getLlmCalls();

    Long getToolCalls();
}
