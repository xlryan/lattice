package com.lattice.core.repository.knowledge;

import com.lattice.core.domain.knowledge.KnowledgeNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface KnowledgeNoteRepository extends JpaRepository<KnowledgeNote, UUID>, KnowledgeNoteRepositoryCustom {
}
