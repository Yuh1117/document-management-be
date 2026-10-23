package com.vpgh.dms.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.vpgh.dms.model.dto.processor.ProcessorRagRequest;
import com.vpgh.dms.service.ProcessorRagService;

import reactor.core.publisher.Flux;

@Service
public class ProcessorRagServiceImpl implements ProcessorRagService {
    private static final Logger log = LoggerFactory.getLogger(ProcessorRagServiceImpl.class);
    private static final ParameterizedTypeReference<ServerSentEvent<String>> SSE_TYPE = new ParameterizedTypeReference<>() {
    };
    private static final String ERROR_PAYLOAD = "{\"message\":\"The answering service is unavailable\"}";

    private final WebClient processorWebClient;

    public ProcessorRagServiceImpl(@Qualifier("processorWebClient") WebClient processorWebClient) {
        this.processorWebClient = processorWebClient;
    }

    @Override
    public Flux<ServerSentEvent<String>> ask(ProcessorRagRequest request) {
        return processorWebClient.post()
                .uri("/rag/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(SSE_TYPE)
                .onErrorResume(e -> {
                    log.error("Processor RAG stream failed", e);
                    return Flux.just(ServerSentEvent.<String>builder().event("error").data(ERROR_PAYLOAD).build());
                });
    }
}
