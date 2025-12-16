package com.lattice.ingest.workflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.lattice.core.domain.DomainType;

/**
 * 调用 LLM 提取结构化 JSONB 的工作流接口。
 */
public interface MetadataExtractionWorkflow {
    JsonNode extract(DomainType domainType, String rawText);
}
