package com.lattice.agent.prompt;

import com.lattice.core.domain.DomainType;

import java.util.Map;

/**
 * 存放 few-shot 提示词，方便不同 Domain 调整语气与 schema。
 */
public final class MetadataPromptLibrary {

    private static final Map<DomainType, String> PROMPTS = Map.of(
            DomainType.CAREER, "输出 STAR 模板 JSON",
            DomainType.DIY, "输出材料/成本 JSON",
            DomainType.MUSIC, "输出和弦/动机 JSON"
    );

    private MetadataPromptLibrary() {
    }

    public static String promptFor(DomainType domainType) {
        return PROMPTS.getOrDefault(domainType, "输出一般备注");
    }
}
