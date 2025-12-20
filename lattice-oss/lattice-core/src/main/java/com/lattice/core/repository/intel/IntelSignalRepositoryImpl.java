package com.lattice.core.repository.intel;

import com.lattice.core.domain.intel.IntelSignal;
import com.lattice.core.repository.support.PgvectorParameter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class IntelSignalRepositoryImpl implements IntelSignalRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<IntelSignal> semanticSearch(List<Double> embedding, int limit) {
        String sql = "select * from lattice.lattice_intel_signals order by embedding <=> :vector";
        Query query = entityManager.createNativeQuery(sql, IntelSignal.class);
        query.setParameter("vector", PgvectorParameter.from(embedding));
        query.setMaxResults(limit);
        @SuppressWarnings("unchecked")
        List<IntelSignal> results = query.getResultList();
        return results;
    }
}
