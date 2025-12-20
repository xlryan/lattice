package com.lattice.core.repository.wealth;

import com.lattice.core.domain.wealth.WealthEntry;
import com.lattice.core.repository.support.PgvectorParameter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class WealthRepositoryImpl implements WealthRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<WealthEntry> semanticSearch(List<Double> embedding, int limit) {
        String sql = "select * from lattice.lattice_wealth_entries order by embedding <=> :vector";
        Query query = entityManager.createNativeQuery(sql, WealthEntry.class);
        query.setParameter("vector", PgvectorParameter.from(embedding));
        query.setMaxResults(limit);
        @SuppressWarnings("unchecked")
        List<WealthEntry> entries = query.getResultList();
        return entries;
    }
}
