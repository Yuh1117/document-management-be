package com.vpgh.dms.service;

import java.util.List;

public interface EmbeddingService {
    List<Double> getEmbedding(String text);
}
