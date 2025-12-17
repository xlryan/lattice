package com.lattice.core.infrastructure.logging;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Ensures every HTTP request carries a trace identifier for observability.
 */
@Component
@Order(1)
public class TraceContextFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(TraceContextFilter.class);
    private static final String HEADER_TRACE_ID = "X-Trace-Id";
    private static final String HEADER_USER_ID = "X-User-Id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String providedTraceId = httpRequest.getHeader(HEADER_TRACE_ID);
        String traceId = TraceContextHolder.ensureTraceId(providedTraceId);
        request.setAttribute(TraceContextHolder.TRACE_ID_KEY, traceId);

        String userId = httpRequest.getHeader(HEADER_USER_ID);
        TraceContextHolder.setUserId(userId);
        if (log.isDebugEnabled()) {
            log.debug("[traceId={}] Incoming request {} {}", traceId, httpRequest.getMethod(), httpRequest.getRequestURI());
        }
        try {
            chain.doFilter(request, response);
        } finally {
            TraceContextHolder.clear();
        }
    }
}
