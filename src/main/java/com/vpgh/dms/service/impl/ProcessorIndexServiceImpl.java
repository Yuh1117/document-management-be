package com.vpgh.dms.service.impl;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vpgh.dms.service.ProcessorIndexService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class ProcessorIndexServiceImpl implements ProcessorIndexService {

    private final RestClient processorRestClient;
    private final ObjectMapper objectMapper;

    public ProcessorIndexServiceImpl(@Qualifier("processorRestClient") RestClient processorRestClient,
            ObjectMapper objectMapper) {
        this.processorRestClient = processorRestClient;
        this.objectMapper = objectMapper;
    }

    @Override
    @Async
    public void deleteIndex(UUID docId) {
        try {
            processorRestClient.delete()
                    .uri("/index/{docId}", docId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new IllegalStateException("Failed to delete ES index for document " + docId + ": " + e.getMessage(),
                    e);
        }
    }

    @Override
    @Async
    public void renameIndex(UUID docId, String name) {
        try {
            String json = objectMapper.writeValueAsString(Map.of("name", name));
            processorRestClient.post()
                    .uri("/index/{docId}/name", docId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .retrieve()
                    .toBodilessEntity();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize rename request for document " + docId, e);
        } catch (RestClientException e) {
            throw new IllegalStateException("Failed to rename ES index for document " + docId + ": " + e.getMessage(),
                    e);
        }
    }
}
