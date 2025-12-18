package com.lattice.retrieval.service;

public interface QueryEmbeddingService {
    float[] toVector(String queryText);
}
