package com.lattice.core.repository.career;

import com.lattice.core.domain.career.CareerNode;
import com.lattice.core.domain.support.DoubleVectorConverter;
import com.pgvector.PGvector;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CareerRepositoryImpl implements CareerRepositoryCustom {

    private final DoubleVectorConverter converter = new DoubleVectorConverter();

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<CareerNode> semanticSearch(List<Double> queryEmbedding, int limit) {
        String sql = "select * from lattice.lattice_career_nodes order by embedding <=> :query_embedding";
        Query query = entityManager.createNativeQuery(sql, CareerNode.class);
        PGvector vector = converter.convertToDatabaseColumn(queryEmbedding);
        if (vector == null) {
            throw new IllegalArgumentException("查询向量不能为空");
        }
        query.setParameter("query_embedding", vector);
        query.setMaxResults(limit);
        @SuppressWarnings("unchecked")
        List<CareerNode> results = query.getResultList();
        return results;
    }
}
