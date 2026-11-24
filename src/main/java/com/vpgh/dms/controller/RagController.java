package com.vpgh.dms.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vpgh.dms.model.dto.processor.ProcessorRagRequest;
import com.vpgh.dms.model.dto.request.ChatConversationRenameReq;
import com.vpgh.dms.model.dto.request.RagAskReq;
import com.vpgh.dms.model.dto.response.ChatConversationRes;
import com.vpgh.dms.model.dto.response.ChatMessagePageRes;
import com.vpgh.dms.model.dto.response.ChatMessageRes;
import com.vpgh.dms.model.entity.ChatConversation;
import com.vpgh.dms.model.entity.ChatMessage;
import com.vpgh.dms.model.entity.SystemSetting;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.repository.DocumentRepository;
import com.vpgh.dms.service.ChatHistoryService;
import com.vpgh.dms.service.ChatHistoryService.MessagePage;
import com.vpgh.dms.service.ProcessorRagService;
import com.vpgh.dms.service.SystemSettingService;
import com.vpgh.dms.util.SecurityUtil;
import com.vpgh.dms.util.exception.TooManyRequestsException;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api")
public class RagController {
    private static final Logger log = LoggerFactory.getLogger(RagController.class);
    private static final int MAX_MESSAGE_PAGE_SIZE = 100;
    /** Earlier messages sent to the model as context. */
    private static final int HISTORY_MESSAGES = 10;
    /** System setting that overrides {@code rag.daily-limit}; 0 or less means no limit. */
    private static final String DAILY_LIMIT_SETTING = "ragDailyLimit";

    private final ProcessorRagService processorRagService;
    private final DocumentRepository documentRepository;
    private final ChatHistoryService chatHistoryService;
    private final ObjectMapper objectMapper;
    private final SystemSettingService systemSettingService;
    private final int defaultDailyLimit;

    public RagController(ProcessorRagService processorRagService, DocumentRepository documentRepository,
            ChatHistoryService chatHistoryService, ObjectMapper objectMapper,
            SystemSettingService systemSettingService,
            @Value("${rag.daily-limit:50}") int defaultDailyLimit) {
        this.processorRagService = processorRagService;
        this.documentRepository = documentRepository;
        this.chatHistoryService = chatHistoryService;
        this.objectMapper = objectMapper;
        this.systemSettingService = systemSettingService;
        this.defaultDailyLimit = defaultDailyLimit;
    }

    @PostMapping(path = "/secure/rag/ask", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> ask(@Valid @RequestBody RagAskReq req, Locale locale) {
        User user = SecurityUtil.getCurrentUserFromThreadLocal();
        String question = req.question().trim();

        // These run before streaming starts, so errors come back as normal JSON responses.
        enforceDailyLimit(user);
        List<ProcessorRagRequest.HistoryMessage> history = chatHistoryService
                .recentHistory(req.conversationId(), user.getId(), HISTORY_MESSAGES).stream()
                .map(this::toHistoryMessage)
                .toList();
        ChatConversation conversation = chatHistoryService.startTurn(user, req.conversationId(), question);
        UUID conversationId = conversation.getId();

        List<UUID> trashed = documentRepository.findDeletedDocumentIdsByOwner(user.getId());
        AnswerAccumulator answer = new AnswerAccumulator();

        ServerSentEvent<String> conversationEvent = ServerSentEvent.<String>builder()
                .event("conversation")
                .data(toJson(Map.of("id", conversationId, "title", conversation.getTitle())))
                .build();

        Flux<ServerSentEvent<String>> answerStream = processorRagService
                .ask(new ProcessorRagRequest(question, user.getId(), locale.getLanguage(), trashed, history))
                .doOnNext(answer::accept);

        return Flux.concat(Flux.just(conversationEvent), answerStream)
                .doFinally(signal -> Schedulers.boundedElastic().schedule(() -> {
                    try {
                        chatHistoryService.saveAssistantMessage(conversationId, answer.content.toString(),
                                answer.sources, answer.finalStatus(signal), answer.model, answer.usage);
                    } catch (Exception e) {
                        log.error("Failed to save assistant message for conversation {}", conversationId, e);
                    }
                }));
    }

    @GetMapping("/secure/rag/conversations")
    public ResponseEntity<List<ChatConversationRes>> getConversations() {
        User user = SecurityUtil.getCurrentUserFromThreadLocal();
        return ResponseEntity.ok(chatHistoryService.getConversations(user.getId()).stream()
                .map(ChatConversationRes::from)
                .toList());
    }

    @GetMapping("/secure/rag/conversations/{id}/messages")
    public ResponseEntity<ChatMessagePageRes> getMessages(@PathVariable UUID id,
            @RequestParam(required = false) UUID before,
            @RequestParam(defaultValue = "30") int limit) {
        User user = SecurityUtil.getCurrentUserFromThreadLocal();
        int pageSize = Math.clamp(limit, 1, MAX_MESSAGE_PAGE_SIZE);
        MessagePage page = chatHistoryService.getMessages(id, user.getId(), before, pageSize);
        return ResponseEntity.ok(new ChatMessagePageRes(
                page.messages().stream().map(ChatMessageRes::from).toList(),
                page.hasMore()));
    }

    @PatchMapping("/secure/rag/conversations/{id}")
    public ResponseEntity<ChatConversationRes> renameConversation(@PathVariable UUID id,
            @Valid @RequestBody ChatConversationRenameReq req) {
        User user = SecurityUtil.getCurrentUserFromThreadLocal();
        return ResponseEntity.ok(ChatConversationRes.from(chatHistoryService.rename(id, user.getId(), req.title())));
    }

    @DeleteMapping("/secure/rag/conversations/{id}")
    public ResponseEntity<Void> deleteConversation(@PathVariable UUID id) {
        User user = SecurityUtil.getCurrentUserFromThreadLocal();
        chatHistoryService.delete(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    private void enforceDailyLimit(User user) {
        int limit = dailyLimit();
        if (limit <= 0) {
            return;
        }
        ZoneId zone = ZoneId.systemDefault();
        long asked = chatHistoryService.countQuestionsSince(user.getId(),
                LocalDate.now(zone).atStartOfDay(zone).toInstant());
        if (asked >= limit) {
            throw new TooManyRequestsException("error.chat.dailyLimit", limit);
        }
    }

    private int dailyLimit() {
        SystemSetting setting = systemSettingService.getSettingByKey(DAILY_LIMIT_SETTING);
        if (setting != null) {
            try {
                return Integer.parseInt(setting.getValue().trim());
            } catch (NumberFormatException e) {
                log.warn("Ignoring invalid {} setting '{}'", DAILY_LIMIT_SETTING, setting.getValue());
            }
        }
        return defaultDailyLimit;
    }

    private ProcessorRagRequest.HistoryMessage toHistoryMessage(ChatMessage m) {
        // The processor trims history to fit the model's input budget.
        String content = m.getContent();
        String used = documentsUsed(m.getSources());
        if (!used.isEmpty()) {
            // Lets follow-ups like "what else does that file say" find the same documents.
            content += "\n\n[Documents used: " + used + "]";
        }
        return new ProcessorRagRequest.HistoryMessage(m.getRole().name().toLowerCase(), content);
    }

    /** "name (id: …), …" for the distinct documents in a stored sources JSON array. */
    private String documentsUsed(String sourcesJson) {
        if (sourcesJson == null || sourcesJson.isBlank()) {
            return "";
        }
        try {
            Map<String, String> documents = new java.util.LinkedHashMap<>();
            for (JsonNode source : objectMapper.readTree(sourcesJson)) {
                String id = source.path("document_id").asText("");
                if (!id.isEmpty()) {
                    documents.putIfAbsent(id, source.path("name").asText(id));
                }
            }
            return documents.entrySet().stream()
                    .map(e -> e.getValue() + " (id: " + e.getKey() + ")")
                    .collect(java.util.stream.Collectors.joining(", "));
        } catch (Exception e) {
            log.warn("Ignoring unreadable sources of a stored message", e);
            return "";
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** Collects the streamed answer so it can be stored once the stream ends. */
    private final class AnswerAccumulator {
        final StringBuilder content = new StringBuilder();
        volatile String sources;
        volatile String model;
        volatile String status;
        volatile String usage;

        void accept(ServerSentEvent<String> event) {
            String type = event.event();
            String data = event.data();
            if (type == null || data == null) {
                return;
            }
            try {
                JsonNode node = objectMapper.readTree(data);
                switch (type) {
                    case "sources" -> sources = node.path("sources").toString();
                    case "token" -> content.append(node.path("text").asText(""));
                    case "done" -> {
                        status = "done";
                        model = node.path("model").isNull() ? null : node.path("model").asText(null);
                        usage = node.path("usage").isObject() ? node.path("usage").toString() : null;
                    }
                    case "error" -> status = "error";
                    default -> {
                    }
                }
            } catch (Exception e) {
                log.warn("Ignoring unparsable RAG event '{}'", type, e);
            }
        }

        String finalStatus(SignalType signal) {
            if (status != null) {
                return status;
            }
            return signal == SignalType.CANCEL ? "stopped" : "error";
        }
    }
}
