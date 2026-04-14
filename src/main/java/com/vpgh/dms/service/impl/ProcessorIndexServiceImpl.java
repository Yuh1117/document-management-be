package com.vpgh.dms.service.impl;

import com.vpgh.dms.service.ProcessorIndexService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class ProcessorIndexServiceImpl implements ProcessorIndexService {

    private final RestClient processorRestClient;

    public ProcessorIndexServiceImpl(@Qualifier("processorRestClient") RestClient processorRestClient) {
        this.processorRestClient = processorRestClient;
    }

    @Override
    public void deleteIndex(Integer docId) {
        try {
            processorRestClient.delete()
                    .uri("/index/{docId}", docId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new IllegalStateException("Failed to delete ES index for document " + docId + ": " + e.getMessage(), e);
        }
    }
}
