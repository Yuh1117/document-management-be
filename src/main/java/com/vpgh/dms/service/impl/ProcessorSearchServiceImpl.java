package com.vpgh.dms.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vpgh.dms.model.dto.processor.ProcessorSearchRequest;
import com.vpgh.dms.model.dto.processor.ProcessorSearchResponse;
import com.vpgh.dms.service.ProcessorSearchService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class ProcessorSearchServiceImpl implements ProcessorSearchService {

    private final RestClient processorRestClient;
    private final ObjectMapper objectMapper;

    public ProcessorSearchServiceImpl(
            @Qualifier("processorRestClient") RestClient processorRestClient,
            ObjectMapper objectMapper) {
        this.processorRestClient = processorRestClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public ProcessorSearchResponse search(ProcessorSearchRequest request) {
        try {
            String json = objectMapper.writeValueAsString(request);
            return processorRestClient.post()
                    .uri("/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .retrieve()
                    .body(ProcessorSearchResponse.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Processor search: failed to serialize request", e);
        } catch (RestClientException e) {
            throw new IllegalStateException("Processor search failed: " + e.getMessage(), e);
        }
    }
}
