package com.lattice.core.repository.query;

import java.util.UUID;

/**
 * 混合检索返回的轻量结果，用于 HTTP 响应或 Agent 拼接。
 */
public record LatticeNodeSearchResult(
        UUID id,
        double score,
        String title,
        String contentSnippet
) {
}
