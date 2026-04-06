package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.dto.processor.ProcessorModelsListResponse;
import com.vpgh.dms.service.ProcessorModelService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class ProcessorModelServiceImpl implements ProcessorModelService {

    private final RestClient processorRestClient;

    public ProcessorModelServiceImpl(@Qualifier("processorRestClient") RestClient processorRestClient) {
        this.processorRestClient = processorRestClient;
    }

    @Override
    public ProcessorModelsListResponse listModels() {
        try {
            return processorRestClient.get()
                    .uri("/models")
                    .retrieve()
                    .body(ProcessorModelsListResponse.class);
        } catch (RestClientException e) {
            throw new IllegalStateException("Processor list models failed: " + e.getMessage(), e);
        }
    }
}
