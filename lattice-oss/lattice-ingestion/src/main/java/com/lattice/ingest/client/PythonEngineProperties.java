package com.lattice.ingest.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "lattice.python-engine")
@Data
public class PythonEngineProperties {
    /**
     * Python 引擎的基础 URL，例如 http://python-worker:8000
     */
    private String baseUrl = "http://python-worker:8000";
}
