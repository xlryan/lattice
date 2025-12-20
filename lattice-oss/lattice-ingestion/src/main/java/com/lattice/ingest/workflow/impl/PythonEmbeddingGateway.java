package com.lattice.ingest.workflow.impl;

import com.lattice.core.infrastructure.client.PythonEngineClient;
import com.lattice.ingest.workflow.EmbeddingGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 基于 Python Engine 的语义向量生成器。
 */
@Component
@Primary
@RequiredArgsConstructor
public class PythonEmbeddingGateway implements EmbeddingGateway {

    private final PythonEngineClient pythonEngineClient;

    @Override
    public List<Double> embed(String text) {
        return pythonEngineClient.embed(text);
    }
}
