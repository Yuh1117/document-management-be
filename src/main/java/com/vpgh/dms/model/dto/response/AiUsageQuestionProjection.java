package com.vpgh.dms.model.dto.response;

/** Questions asked on one day. */
public interface AiUsageQuestionProjection {
    String getDay();

    Long getQuestions();
}
