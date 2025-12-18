package com.lattice.agent.prompt;

import com.lattice.core.domain.DomainType;

import java.util.Map;

/**
 * 存放 few-shot 提示词，方便不同 Domain 调整语气与 schema。
 */
public final class MetadataPromptLibrary {

    private static final Map<DomainType, String> PROMPTS = Map.of(
            DomainType.CAREER, "输出 STAR 模板 JSON",
            DomainType.WEALTH, "输出财务记账 JSON",
            DomainType.BUILD, "输出 BOM/物料 JSON",
            DomainType.KNOWLEDGE, "输出知识笔记 JSON",
            DomainType.INTEL, "输出情报摘要 JSON",
            DomainType.INBOX, "输出简要分类建议 JSON"
    );

    private MetadataPromptLibrary() {
    }

    public static String promptFor(DomainType domainType) {
        return PROMPTS.getOrDefault(domainType, "输出一般备注");
    }
}
