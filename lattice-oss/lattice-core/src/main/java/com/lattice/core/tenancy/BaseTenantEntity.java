package com.lattice.core.tenancy;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

/**
 * Base mapped superclass that injects tenant_id before persisting.
 */
@MappedSuperclass
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@EntityListeners(BaseTenantEntity.TenantColumnListener.class)
public abstract class BaseTenantEntity {

    @Column(name = "tenant_id", nullable = false, updatable = false, length = 64)
    private String tenantId;

    public String getTenantId() {
        return tenantId;
    }

    void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public static class TenantColumnListener {

        @PrePersist
        public void applyTenant(BaseTenantEntity entity) {
            entity.setTenantId(TenantContext.getTenantId());
        }
    }
}
