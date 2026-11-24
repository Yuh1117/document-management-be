package com.vpgh.dms.controller;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vpgh.dms.model.dto.processor.ProcessorRagRequest;
import com.vpgh.dms.model.dto.request.ChatConversationRenameReq;
import com.vpgh.dms.model.dto.request.RagAskReq;
import com.vpgh.dms.model.dto.response.ChatConversationRes;
import com.vpgh.dms.model.dto.response.ChatMessageRes;
import com.vpgh.dms.model.entity.ChatConversation;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.repository.DocumentRepository;
import com.vpgh.dms.service.ChatHistoryService;
import com.vpgh.dms.service.ProcessorRagService;
import com.vpgh.dms.util.SecurityUtil;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api")
public class RagController {
    private static final Logger log = LoggerFactory.getLogger(RagController.class);

    private final ProcessorRagService processorRagService;
    private final DocumentRepository documentRepository;
    private final ChatHistoryService chatHistoryService;
    private final ObjectMapper objectMapper;

    public RagController(ProcessorRagService processorRagService, DocumentRepository documentRepository,
            ChatHistoryService chatHistoryService, ObjectMapper objectMapper) {
        this.processorRagService = processorRagService;
        this.documentRepository = documentRepository;
        this.chatHistoryService = chatHistoryService;
        this.objectMapper = objectMapper;
    }

    @PostMapping(path = "/secure/rag/ask", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> ask(@Valid @RequestBody RagAskReq req, Locale locale) {
        User user = SecurityUtil.getCurrentUserFromThreadLocal();
        String question = req.question().trim();

        // Runs before streaming starts, so an unknown conversation id still fails as a normal 404.
        ChatConversation conversation = chatHistoryService.startTurn(user, req.conversationId(), question);
        UUID conversationId = conversation.getId();

        List<UUID> trashed = documentRepository.findDeletedDocumentIdsByOwner(user.getId());
        AnswerAccumulator answer = new AnswerAccumulator();

        ServerSentEvent<String> conversationEvent = ServerSentEvent.<String>builder()
                .event("conversation")
                .data(toJson(Map.of("id", conversationId, "title", conversation.getTitle())))
                .build();

        Flux<ServerSentEvent<String>> answerStream = processorRagService
                .ask(new ProcessorRagRequest(question, user.getId(), locale.getLanguage(), trashed))
                .doOnNext(answer::accept);

        return Flux.concat(Flux.just(conversationEvent), answerStream)
                .doFinally(signal -> Schedulers.boundedElastic().schedule(() -> {
                    try {
                        chatHistoryService.saveAssistantMessage(conversationId, answer.content.toString(),
                                answer.sources, answer.finalStatus(signal), answer.model);
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
    public ResponseEntity<List<ChatMessageRes>> getMessages(@PathVariable UUID id) {
        User user = SecurityUtil.getCurrentUserFromThreadLocal();
        return ResponseEntity.ok(chatHistoryService.getMessages(id, user.getId()).stream()
                .map(ChatMessageRes::from)
                .toList());
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
