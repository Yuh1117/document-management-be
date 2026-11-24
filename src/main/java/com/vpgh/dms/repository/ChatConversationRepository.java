package com.vpgh.dms.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vpgh.dms.model.entity.ChatConversation;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, UUID> {

    List<ChatConversation> findByUserIdOrderByUpdatedAtDesc(UUID userId);

    Optional<ChatConversation> findByIdAndUserId(UUID id, UUID userId);
}
