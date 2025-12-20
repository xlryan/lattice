package com.lattice.core.infrastructure.logging;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * Simple trace context bridging MDC and request attributes.
 */
public final class TraceContextHolder {

    public static final String TRACE_ID_KEY = "traceId";
    public static final String USER_ID_KEY = "userId";

    private TraceContextHolder() {
    }

    /**
     * 如果传入的 traceId 为空，则生成一个新的；如果不为空，则将其放入 MDC。
     */
    public static String ensureTraceId(String existing) {
        String traceId = existing != null && !existing.isBlank() ? existing : UUID.randomUUID().toString();
        MDC.put(TRACE_ID_KEY, traceId);
        return traceId;
    }

    /**
     * ✅ 新增：手动设置 TraceId (用于异步线程上下文恢复)
     */
    public static void setTraceId(String traceId) {
        if (traceId != null) {
            MDC.put(TRACE_ID_KEY, traceId);
        }
    }

    public static void setUserId(String userId) {
        if (userId != null) {
            MDC.put(USER_ID_KEY, userId);
        }
    }

    public static void clear() {
        MDC.remove(TRACE_ID_KEY);
        MDC.remove(USER_ID_KEY);
    }

    public static String currentTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }

    public static String currentUserId() {
        return MDC.get(USER_ID_KEY);
    }
}