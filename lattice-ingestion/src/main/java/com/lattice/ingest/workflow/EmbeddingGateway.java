package com.lattice.ingest.workflow;

/**
 * 对接 Spring AI EmbeddingClient 的薄封装，便于在单测中 mock。
 */
public interface EmbeddingGateway {

    float[] embed(String text);
}
