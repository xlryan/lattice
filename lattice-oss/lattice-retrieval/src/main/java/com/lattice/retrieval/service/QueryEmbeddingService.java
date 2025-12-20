package com.lattice.retrieval.service;

import java.util.List;

public interface QueryEmbeddingService {
    List<Double> toVector(String queryText);
}
