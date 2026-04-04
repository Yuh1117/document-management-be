package com.vpgh.dms.service.impl;

import com.vpgh.dms.service.ProcessorModelService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Service
public class ProcessorModelServiceImpl implements ProcessorModelService {

    private final RestClient processorRestClient;

    public ProcessorModelServiceImpl(@Qualifier("processorRestClient") RestClient processorRestClient) {
        this.processorRestClient = processorRestClient;
    }

    @Override
    public Map<String, Object> listModels() {
        try {
            return processorRestClient.get()
                    .uri("/models")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
        } catch (RestClientException e) {
            throw new IllegalStateException("Processor list models failed: " + e.getMessage(), e);
        }
    }
}
