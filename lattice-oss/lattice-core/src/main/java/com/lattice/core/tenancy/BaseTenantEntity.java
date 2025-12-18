package com.lattice.core.tenancy;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.springframework.util.StringUtils;

/**
 * Base mapped superclass that injects tenant_id before persisting.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@MappedSuperclass
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@EntityListeners(BaseTenantEntity.TenantColumnListener.class)
public abstract class BaseTenantEntity {

    @Builder.Default
    @Column(name = "tenant_id", nullable = false, updatable = false, length = 64)
    private String tenantId = "default";

    @Builder.Default
    @Column(name = "created_by", nullable = false, length = 64)
    private String createdBy = "system";

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    public static class TenantColumnListener {

        @PrePersist
        public void applyTenant(BaseTenantEntity entity) {
            if (!StringUtils.hasText(entity.getTenantId())) {
                entity.setTenantId(TenantContext.getTenantId());
            }
            if (!StringUtils.hasText(entity.getCreatedBy())) {
                entity.setCreatedBy("system");
            }
        }
    }
}
