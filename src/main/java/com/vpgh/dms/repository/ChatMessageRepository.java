package com.vpgh.dms.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.vpgh.dms.model.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    /** Newest messages first; callers reverse them for display. */
    List<ChatMessage> findByConversationIdOrderByCreatedAtDesc(UUID conversationId, Pageable pageable);

    List<ChatMessage> findByConversationIdAndCreatedAtBeforeOrderByCreatedAtDesc(UUID conversationId,
            Instant before, Pageable pageable);
}
