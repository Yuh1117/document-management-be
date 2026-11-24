package com.vpgh.dms.model.dto.processor;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProcessorRagRequest(
        String question,
        @JsonProperty("owner_id") UUID ownerId,
        String language,
        @JsonProperty("exclude_document_ids") List<UUID> excludeDocumentIds,
        List<HistoryMessage> history
) {

    /** One earlier message of the conversation; role is "user" or "assistant". */
    public record HistoryMessage(String role, String content) {
    }
}
