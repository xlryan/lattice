package com.lattice.core.repository.knowledge;

import com.lattice.core.domain.knowledge.KnowledgeNote;

import java.util.List;

public interface KnowledgeNoteRepositoryCustom {

    List<KnowledgeNote> semanticSearch(List<Double> embedding, int limit);
}
