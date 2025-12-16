package com.lattice.retrieval.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 查询向量生成器，复用与入库一致的 Embedding 模型，保持余弦空间统一。
 */
@Component
@RequiredArgsConstructor
public class SpringAiQueryEmbeddingService implements QueryEmbeddingService {

    private final EmbeddingClient embeddingClient;

    @Override
    public float[] toVector(String queryText) {
        List<Double> data = embeddingClient.embed(queryText);
        float[] vector = new float[data.size()];
        for (int i = 0; i < data.size(); i++) {
            vector[i] = data.get(i).floatValue();
        }
        return vector;
    }
}
