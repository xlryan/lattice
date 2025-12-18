package com.lattice.core.domain.career;

import com.lattice.core.tenancy.BaseTenantEntity;
import com.lattice.core.infrastructure.persistence.VectorType;
import jakarta.persistence.Column;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Career 域的原子节点，承载 STAR 结构和语义向量。
 */
@Entity
@Table(name = "lattice_career_nodes", schema = "lattice")
public class CareerNode extends BaseTenantEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    private CareerType type;

    @Column(name = "raw_content", nullable = false, columnDefinition = "text")
    private String rawContent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "structured_data", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> structuredData;

    @Type(VectorType.class)
    @Column(name = "embedding", nullable = false, columnDefinition = "vector(1536)")
    private List<Double> embedding;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "tags", columnDefinition = "text[]")
    private List<String> tags = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected CareerNode() {
    }

    private CareerNode(Builder builder) {
        this.id = builder.id;
        this.type = builder.type;
        this.rawContent = builder.rawContent;
        this.structuredData = builder.structuredData;
        this.embedding = builder.embedding;
        this.tags = builder.tags;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public CareerType getType() {
        return type;
    }

    public String getRawContent() {
        return rawContent;
    }

    public Map<String, Object> getStructuredData() {
        return structuredData;
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
        private CareerType type;
        private String rawContent;
        private Map<String, Object> structuredData;
        private List<Double> embedding;
        private List<String> tags = new ArrayList<>();

        private Builder() {
        }

        public Builder type(CareerType type) {
            this.type = type;
            return this;
        }

        public Builder rawContent(String rawContent) {
            this.rawContent = rawContent;
            return this;
        }

        public Builder structuredData(Map<String, Object> structuredData) {
            this.structuredData = structuredData;
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

        public CareerNode build() {
            return new CareerNode(this);
        }
    }
}
