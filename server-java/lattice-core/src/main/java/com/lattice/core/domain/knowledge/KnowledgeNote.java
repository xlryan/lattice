package com.lattice.core.domain.knowledge;

import com.lattice.core.domain.support.DoubleVectorConverter;
import com.vladmihalcea.hibernate.type.array.ListArrayType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Knowledge base note that participates in hybrid retrieval.
 */
@Entity
@Table(name = "lattice_knowledge_notes")
public class KnowledgeNote {

    @Id
    private UUID id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Convert(converter = DoubleVectorConverter.class)
    @Column(name = "embedding", columnDefinition = "vector(1536)")
    private List<Double> embedding;

    @Type(ListArrayType.class)
    @Column(name = "tags", columnDefinition = "text[]")
    private List<String> tags;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected KnowledgeNote() {
    }

    private KnowledgeNote(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.content = builder.content;
        this.metadata = builder.metadata;
        this.embedding = builder.embedding;
        this.tags = builder.tags;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public List<Double> getEmbedding() {
        return embedding;
    }

    public List<String> getTags() {
        return tags;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public static final class Builder {
        private UUID id = UUID.randomUUID();
        private String title;
        private String content;
        private Map<String, Object> metadata = Map.of();
        private List<Double> embedding;
        private List<String> tags;

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder embedding(List<Double> embedding) {
            this.embedding = embedding;
            return this;
        }

        public Builder tags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public KnowledgeNote build() {
            return new KnowledgeNote(this);
        }
    }
}
