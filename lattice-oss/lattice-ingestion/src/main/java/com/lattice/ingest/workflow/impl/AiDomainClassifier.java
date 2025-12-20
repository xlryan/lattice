package com.lattice.ingest.workflow.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lattice.core.domain.DomainType;
import com.lattice.ingest.workflow.DomainClassifier;
import com.lattice.ingest.workflow.DomainPrediction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiDomainClassifier implements DomainClassifier {

    // 优化点 1: 增加领域定义的上下文，专门解决 "Project" 的歧义
    private static final String TEMPLATE = """
            # Role
            你是一个精准的文本分类器。请将用户输入归类为以下领域之一：
            
            # Domain Definitions (领域定义)
            1. CAREER (职业): 包含简历、工作日志、软件开发项目(Coding Project)、技术技能(Java/Python)、面试记录。
               * 注意: "开发了一个系统"、"写了一个App" 属于 CAREER。
            2. BUILD (手工/建造): 包含物理世界的 DIY、木工、3D打印、装修、电子硬件焊接。
               * 注意: "做了一个书架"、"装修了厨房" 属于 BUILD。
            3. WEALTH (财富): 记账、发票、投资、资产管理。
            4. KNOWLEDGE (知识): 读书笔记、Obsidian 同步、百科知识、灵感碎片。
            5. INTEL (情报): 新闻、RSS 推送、行业动态。
            6. INBOX (未分类): 无法识别的内容。
            
            # Task
            分析下面的文本，返回一个纯 JSON 对象。
            
            # Output Format (严格遵守)
            不要使用 Markdown (```json)。不要包含任何解释性文字。只返回 JSON。
            
            格式示例: \\{"domain": "CAREER", "confidence": 0.95, "reason": "提到了Java开发"\\}
            
            文本: {input}
            """;

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    // 正则表达式：提取第一个 { 到 最后一个 } 之间的内容，忽略换行符
    private static final Pattern JSON_PATTERN = Pattern.compile("\\{.*\\}", Pattern.DOTALL);

    @Override
    public DomainPrediction classify(String rawText) {
        String content = "";
        try {
            PromptTemplate promptTemplate = new PromptTemplate(TEMPLATE);
            content = chatModel.call(promptTemplate.create(Map.of("input", rawText)))
                    .getResult().getOutput().getText();

            // 优化点 2: 强力清洗 Markdown 和非 JSON 字符
            String jsonContent = cleanAndExtractJson(content);

            log.debug("Cleaned JSON: {}", jsonContent); // 调试用

            JsonNode node = objectMapper.readTree(jsonContent);

            String domainStr = node.path("domain").asText("CAREER").toUpperCase(Locale.ROOT);
            double confidence = node.path("confidence").asDouble(0.8d);

            // 再次校验 Enum 是否存在，防止 AI 幻觉造词
            DomainType domain;
            try {
                domain = DomainType.valueOf(domainStr);
            } catch (IllegalArgumentException e) {
                log.warn("AI 返回了无效的 Domain: {}, 降级为 INBOX", domainStr);
                domain = DomainType.INBOX;
            }

            return new DomainPrediction(domain, confidence);

        } catch (Exception ex) {
            log.error("Domain 分类解析失败。Raw Response: [{}]", content, ex);
            // 降级策略：如果无法解析，默认放入 INBOX 等待人工处理，而不是草率归为 CAREER
            return new DomainPrediction(DomainType.INBOX, 0.0d);
        }
    }

    /**
     * 优化点 3: 鲁棒的 JSON 提取器
     * 即使 AI 返回 ```json { ... } ``` 或者 "结果是: { ... }" 也能正确提取
     */
    private String cleanAndExtractJson(String content) {
        if (content == null) return "{}";

        // 1. 尝试用正则提取最外层的 { ... }
        Matcher matcher = JSON_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group();
        }

        // 2. 如果正则没匹配到（极少情况），尝试暴力清洗
        // 去掉 Markdown 代码块标记
        String cleaned = content.replace("```json", "").replace("```", "").trim();

        // 兜底：如果还不是以 { 开头，可能是一个只有值的字符串，或者完全乱码
        if (!cleaned.startsWith("{")) {
            throw new RuntimeException("无法在响应中找到 JSON 对象");
        }

        return cleaned;
    }
}