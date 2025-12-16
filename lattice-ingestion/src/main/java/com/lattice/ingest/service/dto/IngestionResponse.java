package com.lattice.ingest.service.dto;

import java.util.UUID;

/**
 * 出参只返回节点 ID 与判定的 Domain，方便前端提示。
 */
public record IngestionResponse(UUID nodeId, String domain) {
}
