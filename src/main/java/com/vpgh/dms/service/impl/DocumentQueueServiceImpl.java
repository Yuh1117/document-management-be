package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.service.DocumentQueueService;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DocumentQueueServiceImpl implements DocumentQueueService {

    private final AmqpTemplate amqpTemplate;

    @Value("${rabbitmq.document.exchange}")
    private String exchange;

    @Value("${rabbitmq.document.routing-key}")
    private String routingKey;

    public DocumentQueueServiceImpl(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    @Override
    @Async
    public void publishDocument(Document doc) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("doc_id", doc.getId());
        payload.put("file_url", doc.getFilePath());
        payload.put("file_type", doc.getMimeType());
        payload.put("name", doc.getName());
        payload.put("owner_id", doc.getCreatedBy() != null ? doc.getCreatedBy().getId() : null);

        amqpTemplate.convertAndSend(exchange, routingKey, payload);
    }
}
