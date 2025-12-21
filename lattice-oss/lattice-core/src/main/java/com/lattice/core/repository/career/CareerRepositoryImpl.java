package com.lattice.core.repository.career;

import com.lattice.core.domain.career.CareerNode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import com.lattice.core.repository.support.PgvectorParameter;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CareerRepositoryImpl implements CareerRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<CareerNode> semanticSearch(List<Double> queryEmbedding, int limit) {
        String sql = "select * from lattice.lattice_career_nodes order by embedding <=> cast(:query_embedding as vector)";
        Query query = entityManager.createNativeQuery(sql, CareerNode.class);
        query.setParameter("query_embedding", PgvectorParameter.from(queryEmbedding));
        query.setMaxResults(limit);
        @SuppressWarnings("unchecked")
        List<CareerNode> results = query.getResultList();
        return results;
    }
}
