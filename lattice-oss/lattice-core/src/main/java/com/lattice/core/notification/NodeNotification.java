package com.lattice.core.notification;

import com.lattice.core.domain.DomainType;

import java.time.Instant;
import java.util.UUID;

/**
 * 通知载荷，包含节点元信息，供外部推送服务使用。
 */
public record NodeNotification(
        UUID nodeId,
        DomainType domain,
        String title,
        String preview,
        Instant createdAt
) {
}
