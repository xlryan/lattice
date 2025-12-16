package com.lattice.retrieval.api;

import com.lattice.core.domain.DomainType;
import com.lattice.retrieval.filter.FilterExpression;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 混合检索请求体，支持 JSONB Filter + 语义查询。
 */
public record SearchRequest(
        @NotBlank String query,
        @NotNull DomainType domain,
        FilterExpression filter,
        int topK,
        Double minSimilarity
) {
    public int resolveTopK() {
        return topK > 0 ? Math.min(topK, 50) : 10;
    }

    public double resolveMinSimilarity() {
        return minSimilarity != null ? minSimilarity : 0.3d;
    }
}
