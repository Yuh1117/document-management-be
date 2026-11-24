package com.vpgh.dms.model.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.vpgh.dms.model.entity.ChatConversation;

public record ChatConversationRes(UUID id, String title, Instant createdAt, Instant updatedAt) {

    public static ChatConversationRes from(ChatConversation c) {
        return new ChatConversationRes(c.getId(), c.getTitle(), c.getCreatedAt(), c.getUpdatedAt());
    }
}
