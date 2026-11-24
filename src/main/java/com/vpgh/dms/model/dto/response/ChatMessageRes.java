package com.vpgh.dms.model.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.vpgh.dms.model.entity.ChatMessage;

public record ChatMessageRes(
        UUID id,
        String role,
        String content,
        @JsonRawValue String sources,
        String status,
        String model,
        Instant createdAt
) {

    public static ChatMessageRes from(ChatMessage m) {
        return new ChatMessageRes(
                m.getId(),
                m.getRole().name().toLowerCase(),
                m.getContent(),
                m.getSources(),
                m.getStatus(),
                m.getModel(),
                m.getCreatedAt());
    }
}
