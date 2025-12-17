package com.lattice.core.domain.intel;

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
 * External intelligence signal (RSS/news/insight) persisted for monitoring.
 */
@Entity
@Table(name = "lattice_intel_signals")
public class IntelSignal {

    public enum SignalImportance {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    @Id
    private UUID id;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "headline", nullable = false)
    private String headline;

    @Column(name = "raw_payload", columnDefinition = "text")
    private String rawPayload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "insight", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> insight;

    @Enumerated(EnumType.STRING)
    @Column(name = "importance", nullable = false, length = 16)
    private SignalImportance importance;

    @Convert(converter = DoubleVectorConverter.class)
    @Column(name = "embedding", columnDefinition = "vector(1536)")
    private List<Double> embedding;

    @Type(ListArrayType.class)
    @Column(name = "tags", columnDefinition = "text[]")
    private List<String> tags;

    @CreationTimestamp
    @Column(name = "captured_at", nullable = false, updatable = false)
    private Instant capturedAt;

    protected IntelSignal() {
    }

    private IntelSignal(Builder builder) {
        this.id = builder.id;
        this.source = builder.source;
        this.headline = builder.headline;
        this.rawPayload = builder.rawPayload;
        this.insight = builder.insight;
        this.importance = builder.importance;
        this.embedding = builder.embedding;
        this.tags = builder.tags;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public String getSource() {
        return source;
    }

    public String getHeadline() {
        return headline;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public Map<String, Object> getInsight() {
        return insight;
    }

    public SignalImportance getImportance() {
        return importance;
    }

    public List<Double> getEmbedding() {
        return embedding;
    }

    public List<String> getTags() {
        return tags;
    }

    public Instant getCapturedAt() {
        return capturedAt;
    }

    public static final class Builder {
        private UUID id = UUID.randomUUID();
        private String source;
        private String headline;
        private String rawPayload;
        private Map<String, Object> insight = Map.of();
        private SignalImportance importance = SignalImportance.MEDIUM;
        private List<Double> embedding;
        private List<String> tags;

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public Builder headline(String headline) {
            this.headline = headline;
            return this;
        }

        public Builder rawPayload(String rawPayload) {
            this.rawPayload = rawPayload;
            return this;
        }

        public Builder insight(Map<String, Object> insight) {
            this.insight = insight;
            return this;
        }

        public Builder importance(SignalImportance importance) {
            this.importance = importance;
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

        public IntelSignal build() {
            return new IntelSignal(this);
        }
    }
}
