package com.lattice.core.interfaces.shared;

import java.time.Instant;

/**
 * Canonical API response envelope with tracing metadata.
 */
import com.lattice.core.infrastructure.logging.TraceContextHolder;

public record ApiResponse<T>(String code, String message, T data, String traceId, Instant timestamp) {

    public static <T> ApiResponse<T> success(T data) {
        return success(data, TraceContextHolder.currentTraceId());
    }

    public static <T> ApiResponse<T> success(T data, String traceId) {
        return new ApiResponse<>("SUCCESS", "OK", data, traceId, Instant.now());
    }

    public static <T> ApiResponse<T> failure(String code, String message, String traceId) {
        return new ApiResponse<>(code, message, null, traceId, Instant.now());
    }
}
