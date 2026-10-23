package com.vpgh.dms.controller;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vpgh.dms.model.dto.processor.ProcessorRagRequest;
import com.vpgh.dms.model.dto.request.RagAskReq;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.repository.DocumentRepository;
import com.vpgh.dms.service.ProcessorRagService;
import com.vpgh.dms.util.SecurityUtil;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api")
public class RagController {
    private final ProcessorRagService processorRagService;
    private final DocumentRepository documentRepository;

    public RagController(ProcessorRagService processorRagService, DocumentRepository documentRepository) {
        this.processorRagService = processorRagService;
        this.documentRepository = documentRepository;
    }

    @PostMapping(path = "/secure/rag/ask", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> ask(@Valid @RequestBody RagAskReq req, Locale locale) {
        User user = SecurityUtil.getCurrentUserFromThreadLocal();

        List<UUID> trashed = documentRepository.findDeletedDocumentIdsByOwner(user.getId());

        return processorRagService.ask(
                new ProcessorRagRequest(req.question().trim(), user.getId(), locale.getLanguage(), trashed));
    }
}
