package com.vpgh.dms.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vpgh.dms.model.dto.processor.ProcessorSummarizeRequest;
import com.vpgh.dms.model.dto.processor.ProcessorSummarizeResponse;
import com.vpgh.dms.service.ProcessorSummarizeService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class ProcessorSummarizeServiceImpl implements ProcessorSummarizeService {

    private final RestClient processorRestClient;
    private final ObjectMapper objectMapper;

    public ProcessorSummarizeServiceImpl(
            @Qualifier("processorRestClient") RestClient processorRestClient,
            ObjectMapper objectMapper) {
        this.processorRestClient = processorRestClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public ProcessorSummarizeResponse summarize(ProcessorSummarizeRequest request) {
        try {
            String json = objectMapper.writeValueAsString(request);
            return processorRestClient.post()
                    .uri("/summarize")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .retrieve()
                    .body(ProcessorSummarizeResponse.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Processor summarize: failed to serialize request", e);
        } catch (RestClientException e) {
            throw new IllegalStateException("Processor summarize failed: " + e.getMessage(), e);
        }
    }
}
