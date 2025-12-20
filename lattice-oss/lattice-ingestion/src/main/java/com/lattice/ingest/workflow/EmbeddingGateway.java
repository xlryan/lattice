package com.lattice.ingest.workflow;

import java.util.List;

/**
 * 对接 Spring AI EmbeddingClient 的薄封装，便于在单测中 mock。
 */
public interface EmbeddingGateway {

    List<Double> embed(String text);
}
