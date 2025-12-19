package com.lattice.ingest.client;

import java.util.Map;

public record AnalysisResult(
    String content,
    Map<String, Object> metadata,
    String status
) {}
