package com.lattice.retrieval.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpringAiQueryEmbeddingService implements QueryEmbeddingService {

    private final EmbeddingModel embeddingModel;

    @Override
    public float[] toVector(String queryText) {
        // embed(String) returns float[] in newer Spring AI
        return embeddingModel.embed(queryText);
    }
}
