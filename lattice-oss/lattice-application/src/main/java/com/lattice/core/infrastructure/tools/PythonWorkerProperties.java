package com.lattice.core.infrastructure.tools;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Connection settings for the external Python worker service.
 */
@ConfigurationProperties(prefix = "lattice.python-worker")
public class PythonWorkerProperties {

    private String baseUrl = "http://python-worker:8081";

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
