package com.lattice.core.repository;

import com.lattice.core.domain.support.VectorAttributeConverter;
import com.lattice.core.repository.query.HybridSearchCriteria;
import com.lattice.core.repository.query.LatticeNodeSearchResult;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.postgresql.util.PGobject;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 利用原生 SQL 完成 JSONB + pgvector 的混合检索。
 */
@Repository
public class LatticeNodeQueryRepositoryImpl implements LatticeNodeQueryRepository {

    private final VectorAttributeConverter converter = new VectorAttributeConverter();

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<LatticeNodeSearchResult> hybridSearch(HybridSearchCriteria criteria) {
        StringBuilder sql = new StringBuilder()
                .append("select id, title, substring(content, 1, 360) as snippet, ")
                .append("       (0.7 * (1 - (embedding <=> :query_embedding)) + ")
                .append("        0.3 * coalesce((properties->>'score')::double precision, 0)) as score ")
                .append("from lattice_nodes where domain = :domain");
        if (criteria.filterExpression() != null && !criteria.filterExpression().isBlank()) {
            sql.append(" and (").append(criteria.filterExpression()).append(")");
        }
        sql.append(" order by score desc limit :limit");

        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("domain", criteria.domain().name());
        query.setParameter("limit", criteria.limit());
        PGobject vector = converter.convertToDatabaseColumn(criteria.queryEmbedding());
        if (vector == null) {
            throw new IllegalArgumentException("查询向量不能为空");
        }
        query.setParameter("query_embedding", vector);
        Map<String, Object> params = criteria.parameters();
        if (params != null) {
            params.forEach(query::setParameter);
        }

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        List<LatticeNodeSearchResult> results = new ArrayList<>();
        for (Object[] row : rows) {
            UUID id = (UUID) row[0];
            String title = (String) row[1];
            String snippet = (String) row[2];
            Double score = row[3] instanceof Double d ? d : ((Number) row[3]).doubleValue();
            if (score < criteria.minSimilarity()) {
                continue;
            }
            results.add(new LatticeNodeSearchResult(id, score, title, snippet));
        }
        return results;
    }
}
