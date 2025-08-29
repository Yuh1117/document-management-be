package com.vpgh.dms.service.impl;

import com.vpgh.dms.service.EmbeddingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class EmbeddingServiceImpl implements EmbeddingService {
    private final WebClient webClient;

    public EmbeddingServiceImpl(WebClient.Builder builder, @Value("${embedding.url}") String url) {
        this.webClient = builder.baseUrl(url).build();
    }

    public List<Double> getEmbedding(String text) {
        return webClient.post()
                .uri("/embed")
                .bodyValue(Map.of("text", text))
                .retrieve()
                .bodyToMono(EmbeddingResponse.class)
                .block()
                .getEmbedding();
    }

    static class EmbeddingResponse {
        private List<Double> embedding;

        public List<Double> getEmbedding() {
            return embedding;
        }

        public void setEmbedding(List<Double> embedding) {
            this.embedding = embedding;
        }
    }
}
