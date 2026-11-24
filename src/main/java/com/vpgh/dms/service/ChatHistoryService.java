package com.vpgh.dms.service;

import java.util.List;
import java.util.UUID;

import com.vpgh.dms.model.entity.ChatConversation;
import com.vpgh.dms.model.entity.ChatMessage;
import com.vpgh.dms.model.entity.User;

public interface ChatHistoryService {

    List<ChatConversation> getConversations(UUID userId);

    List<ChatMessage> getMessages(UUID conversationId, UUID userId);

    ChatConversation rename(UUID conversationId, UUID userId, String title);

    void delete(UUID conversationId, UUID userId);

    /**
     * Records the user's question, creating the conversation when {@code conversationId} is null.
     */
    ChatConversation startTurn(User user, UUID conversationId, String question);

    void saveAssistantMessage(UUID conversationId, String content, String sources, String status, String model);
}
