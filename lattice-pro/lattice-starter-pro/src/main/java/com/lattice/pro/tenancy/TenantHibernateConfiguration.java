package com.lattice.pro.tenancy;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers a session-scoped interceptor to enforce tenant filters.
 */
@Configuration
@AutoConfigureAfter(HibernateJpaAutoConfiguration.class)
public class TenantHibernateConfiguration {

    @Bean
    public HibernatePropertiesCustomizer tenantFilterCustomizer() {
        return properties -> properties.put(AvailableSettings.INTERCEPTOR, new TenantFilterInterceptor());
    }
}
