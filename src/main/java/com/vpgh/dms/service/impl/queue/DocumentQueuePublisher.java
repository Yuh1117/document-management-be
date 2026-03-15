package com.vpgh.dms.service.impl.queue;

import com.vpgh.dms.model.entity.Document;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DocumentQueuePublisher {

    private final AmqpTemplate amqpTemplate;

    @Value("${rabbitmq.document.exchange}")
    private String exchange;

    @Value("${rabbitmq.document.routing-key}")
    private String routingKey;

    public DocumentQueuePublisher(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    public void publishDocument(Document doc) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("doc_id", doc.getId());
        payload.put("file_url", doc.getFilePath());
        payload.put("file_type", doc.getMimeType());
        payload.put("owner_id", doc.getCreatedBy() != null ? doc.getCreatedBy().getId() : null);
        payload.put("folder_id", doc.getFolder() != null ? doc.getFolder().getId() : null);

        amqpTemplate.convertAndSend(exchange, routingKey, payload);
    }
}

