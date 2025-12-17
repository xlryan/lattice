package com.lattice.core.repository.wealth;

import com.lattice.core.domain.support.DoubleVectorConverter;
import com.lattice.core.domain.wealth.WealthEntry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.postgresql.util.PGobject;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class WealthRepositoryImpl implements WealthRepositoryCustom {

    private final DoubleVectorConverter converter = new DoubleVectorConverter();

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<WealthEntry> semanticSearch(List<Double> embedding, int limit) {
        String sql = "select * from lattice_wealth_entries order by embedding <=> :vector";
        Query query = entityManager.createNativeQuery(sql, WealthEntry.class);
        PGobject pgVector = converter.convertToDatabaseColumn(embedding);
        if (pgVector == null) {
            throw new IllegalArgumentException("查询向量不能为空");
        }
        query.setParameter("vector", pgVector);
        query.setMaxResults(limit);
        @SuppressWarnings("unchecked")
        List<WealthEntry> entries = query.getResultList();
        return entries;
    }
}
