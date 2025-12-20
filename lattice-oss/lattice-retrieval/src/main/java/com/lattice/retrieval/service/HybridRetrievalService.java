package com.lattice.retrieval.service;

import com.lattice.core.repository.LatticeNodeRepository;
import com.lattice.core.repository.query.HybridSearchCriteria;
import com.lattice.core.repository.query.LatticeNodeSearchResult;
import com.lattice.retrieval.api.SearchRequest;
import com.lattice.retrieval.api.SearchResponse;
import com.lattice.retrieval.api.SearchResultItem;
import com.lattice.retrieval.filter.FilterExpression;
import com.lattice.retrieval.filter.SqlFragment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 混合检索服务，将 FilterExpression 转换为 SQL 再调用仓储。
 */
@Service
@RequiredArgsConstructor
public class HybridRetrievalService implements RetrievalService {

    private final QueryEmbeddingService embeddingService;
    private final LatticeNodeRepository repository;

    @Override
    @Transactional(readOnly = true)
    public SearchResponse search(SearchRequest request) {
        long start = System.currentTimeMillis();
        List<Double> queryVector = embeddingService.toVector(request.query());
        FilterExpression filter = request.filter();
        SqlFragment fragment = filter != null ? filter.toSqlFragment("p") : null;
        Map<String, Object> params = fragment != null ? fragment.parameters() : Collections.emptyMap();

        List<LatticeNodeSearchResult> hits = repository.hybridSearch(new HybridSearchCriteria(
                request.domain(),
                fragment != null ? fragment.expression() : null,
                params,
                queryVector,
                request.resolveTopK(),
                request.resolveMinSimilarity()
        ));

        List<SearchResultItem> payload = hits.stream()
                .map(hit -> new SearchResultItem(hit.id(), hit.title(), hit.contentSnippet(), hit.score()))
                .toList();
        long took = System.currentTimeMillis() - start;
        return new SearchResponse(payload, took);
    }
}
