package com.lattice.pro.tenancy;

import com.lattice.core.tenancy.TenantContext;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Filter;
import org.hibernate.Session;

/**
 * Hibernate interceptor enabling the tenant filter for every Session.
 */
public class TenantFilterInterceptor extends EmptyInterceptor {

    private Session session;

    @Override
    public void setSession(Session session) {
        this.session = session;
        applyTenantFilter();
    }

    private void applyTenantFilter() {
        if (session == null) {
            return;
        }
        Filter filter = session.getEnabledFilter("tenantFilter");
        String tenantId = TenantContext.getTenantId();
        if (filter == null) {
            session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
        } else {
            filter.setParameter("tenantId", tenantId);
        }
    }
}
