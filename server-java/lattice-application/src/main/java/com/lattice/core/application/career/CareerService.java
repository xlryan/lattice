package com.lattice.core.application.career;

import com.lattice.core.application.career.dto.StarRecord;
import com.lattice.core.domain.career.CareerNode;
import com.lattice.core.domain.career.CareerType;
import com.lattice.core.infrastructure.observability.AiUsageMonitor;
import com.lattice.core.infrastructure.prompt.PromptRegistry;
import com.lattice.core.infrastructure.logging.TraceContextHolder;
import com.lattice.core.repository.career.CareerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.PromptTemplateContext;
import org.springframework.ai.chat.prompt.PromptTemplateContextFactory;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.embedding.EmbeddingClient;
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

    private static final String STAR_PROMPT_KEY = "career.star";

    private final CareerRepository repository;
    private final ChatClient chatClient;
    private final EmbeddingClient embeddingClient;
    private final BeanOutputConverter<StarRecord> starOutputConverter = new BeanOutputConverter<>(StarRecord.class);
    private final PromptRegistry promptRegistry;
    private final AiUsageMonitor aiUsageMonitor;

    @Transactional
    public CareerNode createLog(String rawText, CareerType type, List<String> tags) {
        Objects.requireNonNull(type, "type 不能为空");
        log.info("[traceId={}] Creating career node type={} tagCount={}",
                TraceContextHolder.currentTraceId(), type, tags == null ? 0 : tags.size());
        Map<String, Object> structured = generateStarJson(rawText);
        List<Double> embedding = embeddingClient.embed(rawText);
        CareerNode node = CareerNode.builder()
                .type(type)
                .rawContent(rawText)
                .structuredData(structured)
                .embedding(embedding)
                .tags(CollectionUtils.isEmpty(tags) ? List.of() : List.copyOf(tags))
                .build();
        return repository.save(node);
    }

    @Transactional(readOnly = true)
    public List<CareerNode> semanticSearch(String query, int limit) {
        List<Double> vector = embeddingClient.embed(query);
        int resolvedLimit = Math.max(1, Math.min(limit, 20));
        return repository.semanticSearch(vector, resolvedLimit);
    }

    private Map<String, Object> generateStarJson(String rawText) {
        try {
            String prompt = promptRegistry.resolveContent(STAR_PROMPT_KEY);
            PromptTemplate template = new PromptTemplate(prompt);
            PromptTemplateContext context = PromptTemplateContextFactory.map(Map.of("text", rawText));
            var result = chatClient.prompt(template.create(context))
                    .functions(starOutputConverter)
                    .call().getResult();
            var output = result.getOutput();
            String structured = output.getContent();
            recordUsage(result.getMetadata().getUsage(), output.getMetadata().getModel(), "CAREER");
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

    private void recordUsage(org.springframework.ai.model.Usage usage, String model, String domain) {
        if (usage != null) {
            aiUsageMonitor.record(model, domain,
                    usage.getPromptTokens() == null ? 0 : usage.getPromptTokens(),
                    usage.getCompletionTokens() == null ? 0 : usage.getCompletionTokens());
        }
    }
}
