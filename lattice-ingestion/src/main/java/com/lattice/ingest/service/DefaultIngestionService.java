package com.lattice.ingest.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.lattice.core.domain.DomainType;
import com.lattice.core.domain.model.LatticeNode;
import com.lattice.core.repository.LatticeNodeRepository;
import com.lattice.core.service.NodeNormalizationService;
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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

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

    @Override
    @Transactional
    public IngestionResponse ingest(TextIngestionRequest request) {
        DomainPrediction prediction = domainClassifier.classify(request.rawText());
        DomainType domain = prediction.domainType();

        JsonNode metadata = metadataWorkflow.extract(domain, request.rawText());
        JsonNode normalized = normalizationService.normalize(domain, metadata);
        float[] embedding = embeddingGateway.embed(request.rawText());

        LatticeNode node = LatticeNode.builder()
                .id(UUID.randomUUID())
                .domain(domain)
                .title(resolveTitle(request, domain))
                .content(request.rawText())
                .properties(normalized)
                .embedding(embedding)
                .tags(safeTags(request.tags()))
                .build();

        repository.save(node);
        return new IngestionResponse(node.getId(), domain.name());
    }

    private String resolveTitle(TextIngestionRequest request, DomainType domainType) {
        if (StringUtils.hasText(request.titleHint())) {
            return request.titleHint();
        }
        return domainType.name() + " entry";
    }

    private List<String> safeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }
        return tags;
    }
}
