package com.lattice.core.domain.wealth;

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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Wealth domain entry, typically ingested from Firefly or manual bookkeeping.
 */
@Entity
@Table(name = "lattice_wealth_entries", schema = "lattice")
public class WealthEntry extends BaseTenantEntity {

    public enum EntryType {
        EXPENSE,
        INCOME,
        TRANSFER,
        ASSET_EVENT
    }

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 32)
    private EntryType entryType;

    @Column(name = "source_system", length = 64)
    private String sourceSystem;

    @Column(name = "raw_content", columnDefinition = "text", nullable = false)
    private String rawContent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "structured_data", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> structuredData;

    @Column(name = "amount", precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", length = 12)
    private String currency;

    @Column(name = "occurred_on")
    private LocalDate occurredOn;

    @Type(VectorType.class)
    @Column(name = "embedding", columnDefinition = "vector(1536)")
    private List<Double> embedding;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "tags", columnDefinition = "text[]")
    private List<String> tags = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected WealthEntry() {
    }

    private WealthEntry(Builder builder) {
        this.id = builder.id;
        this.entryType = builder.entryType;
        this.sourceSystem = builder.sourceSystem;
        this.rawContent = builder.rawContent;
        this.structuredData = builder.structuredData;
        this.amount = builder.amount;
        this.currency = builder.currency;
        this.occurredOn = builder.occurredOn;
        this.embedding = builder.embedding;
        this.tags = builder.tags;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public EntryType getEntryType() {
        return entryType;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public String getRawContent() {
        return rawContent;
    }

    public Map<String, Object> getStructuredData() {
        return structuredData;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDate getOccurredOn() {
        return occurredOn;
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
        private EntryType entryType;
        private String sourceSystem;
        private String rawContent;
        private Map<String, Object> structuredData = Map.of();
        private BigDecimal amount;
        private String currency;
        private LocalDate occurredOn;
        private List<Double> embedding;
        private List<String> tags = new ArrayList<>();

        public Builder entryType(EntryType entryType) {
            this.entryType = entryType;
            return this;
        }

        public Builder sourceSystem(String sourceSystem) {
            this.sourceSystem = sourceSystem;
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

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder occurredOn(LocalDate occurredOn) {
            this.occurredOn = occurredOn;
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

        public WealthEntry build() {
            return new WealthEntry(this);
        }
    }
}
