package com.lattice.ingest.workflow.impl;

import com.lattice.ingest.workflow.EmbeddingGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 基于 Spring AI EmbeddingClient 的语义向量生成器。
 */
@Component
@RequiredArgsConstructor
public class SpringAiEmbeddingGateway implements EmbeddingGateway {

    private final EmbeddingClient embeddingClient;

    @Override
    public float[] embed(String text) {
        List<Double> embedding = embeddingClient.embed(text);
        float[] vector = new float[embedding.size()];
        for (int i = 0; i < embedding.size(); i++) {
            vector[i] = embedding.get(i).floatValue();
        }
        return vector;
    }
}
