package com.lattice.core.infrastructure.config;

import com.lattice.agent.config.FireflyProperties;
import com.lattice.core.infrastructure.prompt.PromptCatalogProperties;
import com.lattice.core.infrastructure.prompt.PromptRegistry;
import com.lattice.core.infrastructure.security.JwtProperties;
import com.lattice.core.infrastructure.tools.PythonWorkerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Clock;

/**
 * Shared infrastructure beans for the monolith.
 */
@Configuration
@EnableConfigurationProperties({WebCorsProperties.class, PromptCatalogProperties.class,
        PythonWorkerProperties.class, FireflyProperties.class, JwtProperties.class})
public class AppConfig {

    @Bean
    public Clock systemClock() {
        return Clock.systemUTC();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer(WebCorsProperties corsProperties) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(corsProperties.resolvedOrigins().toArray(new String[0]))
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }

    @Bean
    public PromptRegistry promptRegistry(PromptCatalogProperties properties) {
        return new PromptRegistry(properties);
    }
}
