package com.lattice.ingest.workflow;

import com.lattice.core.domain.DomainType;

/**
 * Domain 预测结果，包含置信度，方便后续策略决策。
 */
public record DomainPrediction(DomainType domainType, double confidence) {
}
