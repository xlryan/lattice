package com.lattice.core.repository.knowledge;

import com.lattice.core.domain.knowledge.KnowledgeNote;
import com.lattice.core.domain.support.DoubleVectorConverter;
import com.pgvector.PGvector;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class KnowledgeNoteRepositoryImpl implements KnowledgeNoteRepositoryCustom {

    private final DoubleVectorConverter converter = new DoubleVectorConverter();

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<KnowledgeNote> semanticSearch(List<Double> embedding, int limit) {
        String sql = "select * from lattice.lattice_knowledge_notes order by embedding <=> :vector";
        Query query = entityManager.createNativeQuery(sql, KnowledgeNote.class);
        PGvector pgVector = converter.convertToDatabaseColumn(embedding);
        if (pgVector == null) {
            throw new IllegalArgumentException("查询向量不能为空");
        }
        query.setParameter("vector", pgVector);
        query.setMaxResults(limit);
        @SuppressWarnings("unchecked")
        List<KnowledgeNote> results = query.getResultList();
        return results;
    }
}
