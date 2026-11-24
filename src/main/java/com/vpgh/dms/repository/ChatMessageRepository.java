package com.vpgh.dms.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vpgh.dms.model.constant.ChatRole;
import com.vpgh.dms.model.dto.response.AiUsageDailyProjection;
import com.vpgh.dms.model.dto.response.AiUsageQuestionProjection;
import com.vpgh.dms.model.dto.response.AiUsageUserProjection;
import com.vpgh.dms.model.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    /** Day of a message in the given time zone, as yyyy-MM-dd. */
    String USAGE_DAY = "to_char(m.created_at AT TIME ZONE :tz, 'YYYY-MM-DD')";

    /** Sums of the per-answer usage JSON written by the processor. */
    String USAGE_SUMS = """
            COUNT(*) AS answers,
            COALESCE(SUM((m.usage::jsonb ->> 'input_tokens')::bigint), 0)::bigint AS inputTokens,
            COALESCE(SUM((m.usage::jsonb ->> 'cached_tokens')::bigint), 0)::bigint AS cachedTokens,
            COALESCE(SUM((m.usage::jsonb ->> 'output_tokens')::bigint), 0)::bigint AS outputTokens,
            COALESCE(SUM((m.usage::jsonb ->> 'llm_calls')::bigint), 0)::bigint AS llmCalls,
            COALESCE(SUM((m.usage::jsonb ->> 'tool_calls')::bigint), 0)::bigint AS toolCalls
            """;

    /** Assistant answers with usage data since a given instant. */
    String USAGE_ANSWERS_SINCE = """
            m.role = 'ASSISTANT' AND m.usage IS NOT NULL AND m.created_at >= :since
            """;

    /** Newest messages first; callers reverse them for display. */
    List<ChatMessage> findByConversationIdOrderByCreatedAtDesc(UUID conversationId, Pageable pageable);

    List<ChatMessage> findByConversationIdAndCreatedAtBeforeOrderByCreatedAtDesc(UUID conversationId,
            Instant before, Pageable pageable);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.conversation.user.id = :userId "
            + "AND m.role = :role AND m.createdAt >= :since")
    long countByUserAndRoleSince(@Param("userId") UUID userId, @Param("role") ChatRole role,
            @Param("since") Instant since);

    @Query(value = "SELECT " + USAGE_DAY + " AS day, COALESCE(m.model, '') AS model, " + USAGE_SUMS
            + " FROM chat_messages m WHERE " + USAGE_ANSWERS_SINCE
            + " GROUP BY 1, 2 ORDER BY 1 DESC, 2", nativeQuery = true)
    List<AiUsageDailyProjection> sumUsageByDayAndModel(@Param("tz") String timeZone,
            @Param("since") Instant since);

    @Query(value = "SELECT " + USAGE_DAY + " AS day, COUNT(*) AS questions FROM chat_messages m"
            + " WHERE m.role = 'USER' AND m.created_at >= :since GROUP BY 1 ORDER BY 1 DESC", nativeQuery = true)
    List<AiUsageQuestionProjection> countQuestionsByDay(@Param("tz") String timeZone,
            @Param("since") Instant since);

    @Query(value = """
            SELECT u.id AS userId, u.email AS email,
                   TRIM(CONCAT(u.last_name, ' ', u.first_name)) AS name,
                   COALESCE(m.model, '') AS model,
            """ + USAGE_SUMS + """
            FROM chat_messages m
            JOIN chat_conversations c ON c.id = m.conversation_id
            JOIN users u ON u.id = c.user_id
            WHERE
            """ + USAGE_ANSWERS_SINCE + """
            GROUP BY u.id, u.email, u.last_name, u.first_name, m.model
            """, nativeQuery = true)
    List<AiUsageUserProjection> sumUsageByUserAndModel(@Param("since") Instant since);
}
