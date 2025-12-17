package com.lattice.pro.license;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Performs a simplified JWT-based license verification.
 */
@Service
public class LicenseManager {

    private final SecretKey secretKey = Keys.hmacShaKeyFor(
            "change-this-license-secret-key-32bytes".getBytes(StandardCharsets.UTF_8));
    private final Environment environment;

    public LicenseManager(Environment environment) {
        this.environment = environment;
    }

    /**
     * Verifies a signed license token.
     *
     * @param licenseKey JWT token containing claims about the license.
     */
    public void verifyLicense(String licenseKey) {
        parseClaims(licenseKey);
    }

    /**
     * Resolves maximum allowed users from license claims.
     */
    public int resolveMaxUsers() {
        Claims claims = parseClaims(environment.getProperty("lattice.license.key"));
        Integer allowed = claims.get("max_users", Integer.class);
        return allowed != null ? allowed : 5;
    }

    private Claims parseClaims(String licenseKey) {
        if (licenseKey == null || licenseKey.isBlank()) {
            throw new LicenseInvalidException("License key not configured");
        }
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(licenseKey)
                    .getBody();
        } catch (Exception ex) {
            throw new LicenseInvalidException("License verification failed: " + ex.getMessage());
        }
    }
}
