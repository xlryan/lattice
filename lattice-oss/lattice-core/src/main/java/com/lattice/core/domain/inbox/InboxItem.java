package com.lattice.core.domain.inbox;

import com.lattice.core.domain.DomainType;
import com.lattice.core.tenancy.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Inbox buffer for unclassified inputs awaiting enrichment.
 */
@Entity
@Table(name = "lattice_inbox_items", schema = "lattice")
public class InboxItem extends BaseTenantEntity {

    public enum InboxStatus {
        RECEIVED,
        PROCESSING,
        CLASSIFIED,
        ARCHIVED
    }

    @Id
    private UUID id;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "suggested_domain", length = 32)
    private DomainType suggestedDomain;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private InboxStatus status;

    @CreationTimestamp
    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt;

    protected InboxItem() {
    }

    private InboxItem(Builder builder) {
        this.id = builder.id;
        this.payload = builder.payload;
        this.suggestedDomain = builder.suggestedDomain;
        this.status = builder.status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public DomainType getSuggestedDomain() {
        return suggestedDomain;
    }

    public InboxStatus getStatus() {
        return status;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public static final class Builder {
        private UUID id = UUID.randomUUID();
        private Map<String, Object> payload = Map.of();
        private DomainType suggestedDomain = DomainType.INBOX;
        private InboxStatus status = InboxStatus.RECEIVED;

        public Builder payload(Map<String, Object> payload) {
            this.payload = payload;
            return this;
        }

        public Builder suggestedDomain(DomainType suggestedDomain) {
            this.suggestedDomain = suggestedDomain;
            return this;
        }

        public Builder status(InboxStatus status) {
            this.status = status;
            return this;
        }

        public InboxItem build() {
            return new InboxItem(this);
        }
    }
}
