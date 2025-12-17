package com.lattice.core.repository.build;

import com.lattice.core.domain.build.BuildArtifact;
import com.lattice.core.domain.support.DoubleVectorConverter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.postgresql.util.PGobject;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BuildArtifactRepositoryImpl implements BuildArtifactRepositoryCustom {

    private final DoubleVectorConverter converter = new DoubleVectorConverter();

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<BuildArtifact> semanticSearch(List<Double> embedding, int limit) {
        String sql = "select * from lattice_build_artifacts order by embedding <=> :vector";
        Query query = entityManager.createNativeQuery(sql, BuildArtifact.class);
        PGobject pgVector = converter.convertToDatabaseColumn(embedding);
        if (pgVector == null) {
            throw new IllegalArgumentException("查询向量不能为空");
        }
        query.setParameter("vector", pgVector);
        query.setMaxResults(limit);
        @SuppressWarnings("unchecked")
        List<BuildArtifact> results = query.getResultList();
        return results;
    }
}
