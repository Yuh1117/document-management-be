package com.vpgh.dms.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vpgh.dms.model.constant.ChatRole;
import com.vpgh.dms.model.entity.ChatConversation;
import com.vpgh.dms.model.entity.ChatMessage;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.repository.ChatConversationRepository;
import com.vpgh.dms.repository.ChatMessageRepository;
import com.vpgh.dms.service.ChatHistoryService;
import com.vpgh.dms.util.exception.NotFoundException;

@Service
public class ChatHistoryServiceImpl implements ChatHistoryService {
    private static final int TITLE_MAX_LENGTH = 80;

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;

    public ChatHistoryServiceImpl(ChatConversationRepository conversationRepository,
            ChatMessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatConversation> getConversations(UUID userId) {
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessage> getMessages(UUID conversationId, UUID userId) {
        findOwned(conversationId, userId);
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    @Override
    @Transactional
    public ChatConversation rename(UUID conversationId, UUID userId, String title) {
        ChatConversation conversation = findOwned(conversationId, userId);
        conversation.setTitle(title.trim());
        return conversation;
    }

    @Override
    @Transactional
    public void delete(UUID conversationId, UUID userId) {
        conversationRepository.delete(findOwned(conversationId, userId));
    }

    @Override
    @Transactional
    public ChatConversation startTurn(User user, UUID conversationId, String question) {
        ChatConversation conversation;
        if (conversationId == null) {
            conversation = new ChatConversation();
            conversation.setUser(user);
            conversation.setTitle(toTitle(question));
            conversation = conversationRepository.save(conversation);
        } else {
            conversation = findOwned(conversationId, user.getId());
            conversation.setUpdatedAt(Instant.now());
        }

        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setRole(ChatRole.USER);
        message.setContent(question);
        messageRepository.save(message);

        return conversation;
    }

    @Override
    @Transactional
    public void saveAssistantMessage(UUID conversationId, String content, String sources, String status,
            String model) {
        ChatConversation conversation = conversationRepository.findById(conversationId).orElse(null);
        if (conversation == null) {
            // Deleted while the answer was streaming.
            return;
        }
        conversation.setUpdatedAt(Instant.now());

        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setRole(ChatRole.ASSISTANT);
        message.setContent(content);
        message.setSources(sources);
        message.setStatus(status);
        message.setModel(model);
        messageRepository.save(message);
    }

    private ChatConversation findOwned(UUID conversationId, UUID userId) {
        return conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new NotFoundException("error.chat.conversationNotFound"));
    }

    private static String toTitle(String question) {
        String oneLine = question.strip().replaceAll("\\s+", " ");
        return oneLine.length() <= TITLE_MAX_LENGTH ? oneLine : oneLine.substring(0, TITLE_MAX_LENGTH - 1) + "…";
    }
}
