package com.lattice.retrieval.service;

import com.lattice.core.infrastructure.client.PythonEngineClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Primary
@RequiredArgsConstructor
public class PythonQueryEmbeddingService implements QueryEmbeddingService {

    private final PythonEngineClient pythonEngineClient;

    @Override
    public List<Double> toVector(String queryText) {
        return pythonEngineClient.embed(queryText);
    }
}
