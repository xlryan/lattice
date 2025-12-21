package com.lattice.core.application.career;

import com.lattice.core.application.career.dto.StarRecord;
import com.lattice.core.domain.career.CareerNode;
import com.lattice.core.domain.career.CareerType;
import com.lattice.core.domain.support.VectorUtils;
import com.lattice.core.infrastructure.client.PythonEngineClient;
import com.lattice.core.infrastructure.observability.AiUsageMonitor;
import com.lattice.core.infrastructure.prompt.PromptRegistry;
import com.lattice.core.infrastructure.logging.TraceContextHolder;
import com.lattice.core.repository.career.CareerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CareerService {

    private static final String STAR_PROMPT_KEY = "career-star";

    private final CareerRepository repository;
    private final ChatModel chatModel;
    private final PythonEngineClient pythonEngineClient;
    private final BeanOutputConverter<StarRecord> starOutputConverter = new BeanOutputConverter<>(StarRecord.class);
    private final PromptRegistry promptRegistry;
    private final AiUsageMonitor aiUsageMonitor;

    @Transactional
    public CareerNode createLog(String rawText, CareerType type, List<String> tags) {
        Objects.requireNonNull(type, "type 不能为空");
        log.info("[traceId={}] Creating/Updating career node type={} tagCount={}",
                TraceContextHolder.currentTraceId(), type, tags == null ? 0 : tags.size());
        
        List<Double> embedding = pythonEngineClient.embed(rawText);
        
        // 1. 语义查重：检查是否已有雷同内容 (相似度阈值 0.90)
        List<CareerNode> existing = repository.semanticSearch(embedding, 1);
        if (!existing.isEmpty()) {
            CareerNode topMatch = existing.get(0);
            double similarity = calculateSimilarity(embedding, topMatch.getEmbedding());
            if (similarity > 0.90) {
                log.info("Detected similar career node (similarity={}), refining existing record instead of creating new.", similarity);
                
                // 只有当新文本更长或显著不同时才更新，或者强制更新以重新提取结构
                Map<String, Object> structured = generateStarJson(rawText);
                topMatch.updateContent(rawText, structured, VectorUtils.toFloatArray(embedding), tags);
                return repository.save(topMatch);
            }
        }

        Map<String, Object> structured = generateStarJson(rawText);
        CareerNode node = CareerNode.builder()
                .type(type)
                .rawContent(rawText)
                .structuredData(structured)
                .embedding(embedding)
                .tags(CollectionUtils.isEmpty(tags) ? List.of() : List.copyOf(tags))
                .build();
        return repository.save(node);
    }

    private double calculateSimilarity(List<Double> v1, float[] v2) {
        if (v1.size() != v2.length) return 0.0;
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < v1.size(); i++) {
            dotProduct += v1.get(i) * v2[i];
            normA += Math.pow(v1.get(i), 2);
            normB += Math.pow(v2[i], 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    @Transactional(readOnly = true)
    public List<CareerNode> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<CareerNode> semanticSearch(String query, int limit) {
        List<Double> vector = pythonEngineClient.embed(query);
        int resolvedLimit = Math.max(1, Math.min(limit, 20));
        return repository.semanticSearch(vector, resolvedLimit);
    }

    private Map<String, Object> generateStarJson(String rawText) {
        try {
            String prompt = promptRegistry.resolveContent(STAR_PROMPT_KEY);
            PromptTemplate template = new PromptTemplate(prompt);
            ChatResponse response = chatModel.call(template.create(Map.of("text", rawText)));
            Generation result = response.getResult();
            String structured = result.getOutput().getText();
            var metadata = response.getMetadata();
            String model = metadata == null ? null : metadata.getModel();
            recordUsage(metadata == null ? null : metadata.getUsage(), model, "CAREER");
            StarRecord record = starOutputConverter.convert(structured);
            return Map.of(
                    "situation", record.situation(),
                    "task", record.task(),
                    "action", record.action(),
                    "result", record.result()
            );
        } catch (Exception ex) {
            log.warn("STAR 提取失败，返回默认结构", ex);
            return Map.of(
                    "situation", rawText,
                    "task", "",
                    "action", "",
                    "result", ""
            );
        }
    }

    private void recordUsage(Usage usage, String model, String domain) {
        if (usage != null) {
            aiUsageMonitor.record(model, domain,
                    usage.getPromptTokens() == null ? 0 : usage.getPromptTokens(),
                    usage.getCompletionTokens() == null ? 0 : usage.getCompletionTokens());
        }
    }
}
