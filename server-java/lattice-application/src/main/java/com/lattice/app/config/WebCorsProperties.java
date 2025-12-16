package com.lattice.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CORS 相关配置，支持多来源自定义。
 */
@ConfigurationProperties(prefix = "lattice.web.cors")
public class WebCorsProperties {

    private String allowedOrigins = "http://localhost:3000,http://localhost:4173";

    public List<String> resolvedOrigins() {
        return Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public String getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(String allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }
}
