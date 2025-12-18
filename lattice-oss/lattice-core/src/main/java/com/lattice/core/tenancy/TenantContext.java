package com.lattice.core.tenancy;

/**
 * ThreadLocal tenant context used by shared-database multi-tenancy.
 * OSS builds default to "default-tenant" while commercial builds
 * override the value per request.
 */
public final class TenantContext {

    private static final ThreadLocal<String> TENANT_HOLDER = ThreadLocal.withInitial(() -> "default-tenant");

    private TenantContext() {
    }

    public static String getTenantId() {
        return TENANT_HOLDER.get();
    }

    public static void setTenantId(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            TENANT_HOLDER.set("default-tenant");
        } else {
            TENANT_HOLDER.set(tenantId);
        }
    }

    public static void clear() {
        TENANT_HOLDER.remove();
    }
}
