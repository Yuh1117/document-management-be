package com.vpgh.dms.model.dto.response;

import java.util.List;
import java.util.UUID;

/**
 * Token usage of the document assistant over a period, for the admin page.
 * Rows are split by model so the client can price each model separately.
 */
public record AiUsageRes(
        int days,
        List<DailyRow> daily,
        List<QuestionRow> questions,
        List<UserRow> users
) {

    /** Answers of one model on one day ({@code date} is yyyy-MM-dd in the server time zone). */
    public record DailyRow(String date, String model, long answers, long inputTokens, long cachedTokens,
            long outputTokens, long llmCalls, long toolCalls) {
    }

    /** Questions asked on one day, including those whose answer failed. */
    public record QuestionRow(String date, long questions) {
    }

    /** Answers one user received from one model. */
    public record UserRow(UUID userId, String email, String name, String model, long answers,
            long inputTokens, long cachedTokens, long outputTokens) {
    }
}
