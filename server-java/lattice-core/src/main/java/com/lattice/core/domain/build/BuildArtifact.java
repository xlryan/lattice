package com.lattice.core.domain.build;

import com.lattice.core.domain.support.DoubleVectorConverter;
import com.vladmihalcea.hibernate.type.array.ListArrayType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Represents an artifact or inspiration within the BUILD domain.
 */
@Entity
@Table(name = "lattice_build_artifacts")
public class BuildArtifact {

    public enum ArtifactCategory {
        PROJECT,
        BOM,
        INSPIRATION
    }

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 32)
    private ArtifactCategory category;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attributes", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> attributes;

    @Convert(converter = DoubleVectorConverter.class)
    @Column(name = "embedding", columnDefinition = "vector(1536)")
    private List<Double> embedding;

    @Type(ListArrayType.class)
    @Column(name = "tags", columnDefinition = "text[]")
    private List<String> tags;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected BuildArtifact() {
    }

    private BuildArtifact(Builder builder) {
        this.id = builder.id;
        this.category = builder.category;
        this.title = builder.title;
        this.description = builder.description;
        this.attributes = builder.attributes;
        this.embedding = builder.embedding;
        this.tags = builder.tags;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public ArtifactCategory getCategory() {
        return category;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
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
        private ArtifactCategory category;
        private String title;
        private String description;
        private Map<String, Object> attributes = Map.of();
        private List<Double> embedding;
        private List<String> tags;

        public Builder category(ArtifactCategory category) {
            this.category = category;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder attributes(Map<String, Object> attributes) {
            this.attributes = attributes;
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

        public BuildArtifact build() {
            return new BuildArtifact(this);
        }
    }
}
