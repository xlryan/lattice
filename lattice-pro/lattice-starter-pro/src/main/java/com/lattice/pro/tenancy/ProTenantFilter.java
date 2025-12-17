package com.lattice.pro.tenancy;

import com.lattice.core.tenancy.TenantContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Extracts tenant identifier from JWT tokens and binds it to {@link TenantContext}.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ProTenantFilter extends OncePerRequestFilter {

    private final SecretKey secretKey = Keys.hmacShaKeyFor(
            "multi-tenant-secret-change-me-please-32byte".getBytes(StandardCharsets.UTF_8));

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String tenantId = resolveTenantId(request);
            TenantContext.setTenantId(tenantId);
            filterChain.doFilter(request, response);
        } catch (TenantMissingException ex) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, ex.getMessage());
        } finally {
            TenantContext.clear();
        }
    }

    private String resolveTenantId(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
            throw new TenantMissingException("Missing authorization token");
        }
        String token = header.substring(7);
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        String tenantId = claims.get("tenant_id", String.class);
        if (!StringUtils.hasText(tenantId)) {
            throw new TenantMissingException("tenant_id claim absent");
        }
        return tenantId;
    }

    private static class TenantMissingException extends RuntimeException {
        TenantMissingException(String message) {
            super(message);
        }
    }
}
