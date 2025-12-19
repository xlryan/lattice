package com.lattice.ingest.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.lattice.core.domain.DomainType;
import com.lattice.core.domain.model.LatticeNode;
import com.lattice.core.notification.NodeNotification;
import com.lattice.core.notification.NotificationPublisher;
import com.lattice.core.repository.LatticeNodeRepository;
import com.lattice.core.service.NodeNormalizationService;
import com.lattice.ingest.client.AnalysisResult;
import com.lattice.ingest.client.PythonEngineClient;
import com.lattice.ingest.service.dto.IngestionResponse;
import com.lattice.ingest.service.dto.TextIngestionRequest;
import com.lattice.ingest.workflow.DomainClassifier;
import com.lattice.ingest.workflow.DomainPrediction;
import com.lattice.ingest.workflow.EmbeddingGateway;
import com.lattice.ingest.workflow.MetadataExtractionWorkflow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 串联分类、抽取、嵌入和持久化流程，确保落库数据一致。
 */
@Service
@RequiredArgsConstructor
public class DefaultIngestionService implements IngestionService {

    private final DomainClassifier domainClassifier;
    private final MetadataExtractionWorkflow metadataWorkflow;
    private final NodeNormalizationService normalizationService;
    private final EmbeddingGateway embeddingGateway;
    private final LatticeNodeRepository repository;
    private final ObjectProvider<NotificationPublisher> notificationPublisher;
    private final PythonEngineClient pythonEngineClient;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public IngestionResponse ingest(TextIngestionRequest request) {
        DomainPrediction prediction = domainClassifier.classify(request.rawText());
        return processIngestion(prediction.domainType(), request.rawText(), request.titleHint(), request.tags());
    }

    @Override
    @Transactional
    public IngestionResponse ingestFile(MultipartFile file, String titleHint, List<String> tags) {
        AnalysisResult analysis = pythonEngineClient.analyze(file.getResource());
        
        // 基于 Python 返回的内容重新进行领域分类
        DomainPrediction prediction = domainClassifier.classify(analysis.content());
        DomainType domain = prediction.domainType();
        
        // 融合 Python 提取的元数据与 AI 抽取的元数据
        JsonNode pythonMetadata = objectMapper.valueToTree(analysis.metadata());
        JsonNode aiMetadata = metadataWorkflow.extract(domain, analysis.content());
        
        // 简单合并策略：优先使用 Python 引擎提取的元数据
        JsonNode merged = mergeMetadata(pythonMetadata, aiMetadata);
        
        return processIngestion(domain, analysis.content(), 
            StringUtils.hasText(titleHint) ? titleHint : file.getOriginalFilename(), tags);
    }

    private IngestionResponse processIngestion(DomainType domain, String content, String titleHint, List<String> tags) {
        JsonNode metadata = metadataWorkflow.extract(domain, content);
        JsonNode normalized = normalizationService.normalize(domain, metadata);
        float[] embedding = embeddingGateway.embed(content);

        LatticeNode node = LatticeNode.builder()
                .id(UUID.randomUUID())
                .domain(domain)
                .title(resolveTitle(titleHint, domain))
                .content(content)
                .properties(normalized)
                .embedding(embedding)
                .tags(safeTags(tags))
                .build();

        repository.save(node);
        notifyNodeCreated(node);
        return new IngestionResponse(node.getId(), domain.name());
    }

    private JsonNode mergeMetadata(JsonNode main, JsonNode secondary) {
        // 简单逻辑：如果 main 是空的，用 secondary
        return (main != null && !main.isEmpty()) ? main : secondary;
    }

    private String resolveTitle(String titleHint, DomainType domainType) {
        if (StringUtils.hasText(titleHint)) {
            return titleHint;
        }
        return domainType.name() + " entry";
    }


    private List<String> safeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }
        return tags;
    }

    private void notifyNodeCreated(LatticeNode node) {
        notificationPublisher.ifAvailable(publisher ->
                publisher.publish(new NodeNotification(
                        node.getId(),
                        node.getDomain(),
                        node.getTitle(),
                        buildPreview(node.getContent()),
                        Instant.now()
                )));
    }

    private String buildPreview(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        String trimmed = content.trim();
        return trimmed.length() > 160 ? trimmed.substring(0, 160) + "..." : trimmed;
    }
}
