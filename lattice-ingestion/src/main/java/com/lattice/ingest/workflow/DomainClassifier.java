package com.lattice.ingest.workflow;

/**
 * 文本分类接口，通过 AI or 规则识别 Domain。
 */
public interface DomainClassifier {
    DomainPrediction classify(String rawText);
}
