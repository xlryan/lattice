package com.lattice.core.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT secret/issuer configuration.
 */
@ConfigurationProperties(prefix = "lattice.security.jwt")
public class JwtProperties {

    private String secret = "change-me";
    private String issuer = "lattice";
    private long expirySeconds = 86400;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public long getExpirySeconds() {
        return expirySeconds;
    }

    public void setExpirySeconds(long expirySeconds) {
        this.expirySeconds = expirySeconds;
    }
}
