package com.lattice.core.infrastructure.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "lattice.python-worker")
@Data
public class PythonEngineProperties {
    private String baseUrl = "http://127.0.0.1:8000";
}
