package com.vpgh.dms.model.dto.response;

import java.util.List;

/**
 * One page of a conversation, oldest first. {@code hasMore} is true when older messages exist.
 */
public record ChatMessagePageRes(List<ChatMessageRes> messages, boolean hasMore) {
}
