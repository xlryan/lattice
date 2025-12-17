package com.lattice.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lattice.core.domain.DomainType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 对 AI 抽取出的元数据做二次加工，例如成本归一化、缺省字段补全。
 */
@Component
@RequiredArgsConstructor
public class NodeNormalizationService {

    private final ObjectMapper objectMapper;

    /**
     * @return 规整后的 JSONB，所有数字字段均转为 double，便于生成表达式索引
     */
    public JsonNode normalize(DomainType domainType, JsonNode source) {
        ObjectNode target = source.deepCopy();
        if (domainType == DomainType.BUILD && source.has("cost")) {
            double standardized = source.path("cost").asDouble(0.0d);
            target.put("cost", standardized);
        }
        if (domainType == DomainType.CAREER && !source.has("pattern")) {
            target.put("pattern", "STAR");
        }
        return target;
    }
}
