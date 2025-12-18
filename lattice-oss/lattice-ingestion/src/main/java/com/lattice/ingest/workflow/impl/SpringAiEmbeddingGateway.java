package com.lattice.ingest.workflow.impl;

import com.lattice.ingest.workflow.EmbeddingGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 基于 Spring AI EmbeddingModel 的语义向量生成器。
 */
@Component
@RequiredArgsConstructor
public class SpringAiEmbeddingGateway implements EmbeddingGateway {

    private final EmbeddingModel embeddingModel;

    @Override
    public float[] embed(String text) {
        return embeddingModel.embed(text);
    }
}
