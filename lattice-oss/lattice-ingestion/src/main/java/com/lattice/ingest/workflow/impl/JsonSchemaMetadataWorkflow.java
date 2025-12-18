package com.lattice.ingest.workflow.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lattice.core.domain.DomainType;
import com.lattice.ingest.workflow.MetadataExtractionWorkflow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 通过 Json Schema few-shot 引导 LLM 输出 JSONB，必要时降级为空对象。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JsonSchemaMetadataWorkflow implements MetadataExtractionWorkflow {

    private static final Map<DomainType, String> SCHEMAS = Map.of(
            DomainType.CAREER, "{situation, task, action, result, skills}",
            DomainType.WEALTH, "{booked_at, amount, currency, category, source_account, notes}",
            DomainType.BUILD, "{materials[], cost, tools, bom_reference, notes}",
            DomainType.KNOWLEDGE, "{topics[], summary, references[], source_url}",
            DomainType.INTEL, "{source, summary, risk_level, recommended_action}",
            DomainType.INBOX, "{raw_hint, recommended_domain, urgency}"
    );

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    @Override
    public JsonNode extract(DomainType domainType, String rawText) {
        try {
            String schema = SCHEMAS.getOrDefault(domainType, "{notes}");
            PromptTemplate template = new PromptTemplate("""
                    依据 schema 输出 JSON，不要加解释。\n
                    schema: {schema}\n
                    文本: {input}
                    """);
            String content = chatModel.call(template.create(Map.of(
                    "schema", schema,
                    "input", rawText
            ))).getResult().getOutput().getText();
            return objectMapper.readTree(content);
        } catch (Exception ex) {
            log.warn("元数据抽取失败，降级为空 JSON", ex);
            return objectMapper.createObjectNode();
        }
    }
}
