package com.lattice.core.infrastructure.client;

import java.util.List;
import java.util.Map;

public record AnalysisResult(
    String content,
    Map<String, Object> metadata,
    String status,
    List<Double> vector,
    Integer vectorDim,
    List<String> keywords
) {}
