package com.vpgh.dms.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.vpgh.dms.model.entity.ChatConversation;
import com.vpgh.dms.model.entity.ChatMessage;
import com.vpgh.dms.model.entity.User;

public interface ChatHistoryService {

    record MessagePage(List<ChatMessage> messages, boolean hasMore) {
    }

    List<ChatConversation> getConversations(UUID userId);

    /**
     * Returns up to {@code limit} messages older than {@code beforeMessageId} (or the latest ones when null),
     * oldest first, and whether even older messages exist.
     */
    MessagePage getMessages(UUID conversationId, UUID userId, UUID beforeMessageId, int limit);

    ChatConversation rename(UUID conversationId, UUID userId, String title);

    void delete(UUID conversationId, UUID userId);

    /**
     * Records the user's question, creating the conversation when {@code conversationId} is null.
     */
    ChatConversation startTurn(User user, UUID conversationId, String question);

    void saveAssistantMessage(UUID conversationId, String content, String sources, String status, String model,
            String usage);

    /**
     * The latest messages of a conversation to give the model as context, oldest first.
     * Unfinished or failed answers are left out. Empty for a new conversation.
     */
    List<ChatMessage> recentHistory(UUID conversationId, UUID userId, int limit);

    /** Questions the user has asked since the given instant, across all conversations. */
    long countQuestionsSince(UUID userId, Instant since);
}
