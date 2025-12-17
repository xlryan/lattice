package com.lattice.core.repository.query;

import com.lattice.core.domain.DomainType;

import java.util.Map;

/**
 * 组合 JSONB 过滤与语义向量搜索的查询入参。
 */
public record HybridSearchCriteria(
        DomainType domain,
        String filterExpression,
        Map<String, Object> parameters,
        float[] queryEmbedding,
        int limit,
        double minSimilarity
) {
}
