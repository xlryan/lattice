package com.lattice.agent.finance.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 封装一笔支出指令，供 API 客户端统一构造请求。
 */
@Builder
public record FireflyExpenseCommand(
        BigDecimal amount,
        String description,
        String category,
        String sourceAccount,
        String destinationAccount,
        OffsetDateTime occurredAt,
        String currency
) {
}
