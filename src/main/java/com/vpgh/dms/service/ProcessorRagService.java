package com.vpgh.dms.service;

import org.springframework.http.codec.ServerSentEvent;

import com.vpgh.dms.model.dto.processor.ProcessorRagRequest;

import reactor.core.publisher.Flux;

public interface ProcessorRagService {

    Flux<ServerSentEvent<String>> ask(ProcessorRagRequest request);
}
