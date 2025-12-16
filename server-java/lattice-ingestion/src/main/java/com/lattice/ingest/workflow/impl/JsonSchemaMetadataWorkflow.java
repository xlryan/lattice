package com.lattice.ingest.workflow.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lattice.core.domain.DomainType;
import com.lattice.ingest.workflow.MetadataExtractionWorkflow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
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
            DomainType.DIY, "{materials[], cost, tools, notes, constraints}",
            DomainType.MUSIC, "{key, tempo_bpm, motif, chords[], mood[]}",
            DomainType.FINANCE, "{booked_at, amount, currency, category, source_account, notes}"
    );

    private final ChatClient chatClient;
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
            String content = chatClient.call(template.create(Map.of(
                    "schema", schema,
                    "input", rawText
            ))).getResult().getOutput().getContent();
            return objectMapper.readTree(content);
        } catch (Exception ex) {
            log.warn("元数据抽取失败，降级为空 JSON", ex);
            return objectMapper.createObjectNode();
        }
    }
}
