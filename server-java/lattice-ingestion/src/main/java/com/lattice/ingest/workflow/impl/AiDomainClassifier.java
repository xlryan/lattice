package com.lattice.ingest.workflow.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lattice.core.domain.DomainType;
import com.lattice.ingest.workflow.DomainClassifier;
import com.lattice.ingest.workflow.DomainPrediction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

/**
 * 使用 Spring AI ChatClient 调用 DeepSeek/OpenAI，实现 zero-shot Domain 分类。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiDomainClassifier implements DomainClassifier {

    private static final String TEMPLATE = """
            你是一位分类器，请将用户输入划分到 CAREER/WEALTH/BUILD/KNOWLEDGE/INTEL/INBOX 之一。
            输出 JSON: {\"domain\": string, \"confidence\": 0-1}.
            文本: {input}
            """;

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    @Override
    public DomainPrediction classify(String rawText) {
        try {
            PromptTemplate promptTemplate = new PromptTemplate(TEMPLATE);
            String content = chatClient.call(promptTemplate.create(Map.of("input", rawText)))
                    .getResult().getOutput().getContent();
            JsonNode node = objectMapper.readTree(content);
            DomainType domain = DomainType.valueOf(node.path("domain").asText("CAREER").toUpperCase(Locale.ROOT));
            double confidence = node.path("confidence").asDouble(0.8d);
            return new DomainPrediction(domain, confidence);
        } catch (Exception ex) {
            log.warn("Domain 分类失败，使用默认 CAREER", ex);
            return new DomainPrediction(DomainType.CAREER, 0.5d);
        }
    }
}
