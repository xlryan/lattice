package com.lattice.core.domain.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.lattice.core.domain.DomainType;
import com.lattice.core.domain.support.VectorAttributeConverter;
import com.vladmihalcea.hibernate.type.array.ListArrayType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Lattice 的最小知识单元，融合文本、JSON 元数据与语义向量。
 */
@Entity
@Table(name = "lattice_nodes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LatticeNode {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "domain", nullable = false, length = 32)
    private DomainType domain;

    @NotBlank
    @Column(name = "title", nullable = false)
    private String title;

    @NotBlank
    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    @Type(JsonBinaryType.class)
    @Column(name = "properties", nullable = false, columnDefinition = "jsonb")
    private JsonNode properties;

    @Convert(converter = VectorAttributeConverter.class)
    @Column(name = "embedding", columnDefinition = "vector(1536)")
    private float[] embedding;

    @Type(ListArrayType.class)
    @Column(name = "tags", columnDefinition = "text[]")
    private List<String> tags;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public boolean hasEmbedding() {
        return embedding != null && embedding.length > 0;
    }

    public boolean hasTags() {
        return tags != null && !tags.isEmpty();
    }
}
