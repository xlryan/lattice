package com.lattice.core.repository.build;

import com.lattice.core.domain.build.BuildArtifact;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BuildArtifactRepositoryImpl implements BuildArtifactRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<BuildArtifact> semanticSearch(List<Double> embedding, int limit) {
        // 1. 将 List<Double> 转换为 float[]
        // Hibernate Vector 原生偏好 float[]，这样性能最好
        float[] vectorArray = new float[embedding.size()];
        for (int i = 0; i < embedding.size(); i++) {
            vectorArray[i] = embedding.get(i).floatValue();
        }

        // 2. 使用 JPQL 查询
        // l2_distance 对应 SQL 中的 <=> 操作符
        String hpql = "select b from BuildArtifact b order by l2_distance(b.embedding, :vector)";

        return entityManager.createQuery(hpql, BuildArtifact.class)
                .setParameter("vector", vectorArray)
                .setMaxResults(limit)
                .getResultList();
    }
}