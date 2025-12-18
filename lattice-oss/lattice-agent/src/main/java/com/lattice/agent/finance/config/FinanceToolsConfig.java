package com.lattice.agent.finance.config;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.lattice.agent.finance.model.FireflyExpenseCommand;
import com.lattice.agent.finance.service.FinanceCategoryMapper;
import com.lattice.agent.finance.service.FireflyApiClient;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.function.Function;

/**
 * 声明 Spring AI Function Callback，让大模型具备记账执行能力。
 */
@Configuration
public class FinanceToolsConfig {

    public record TransactionRequest(
            @JsonPropertyDescription("金额，单位为元") double amount,
            @JsonPropertyDescription("交易描述，例如：买咖啡") String description,
            @JsonPropertyDescription("类别，如：餐饮、交通、数码") String category,
            @JsonPropertyDescription("源账户，默认是现金或信用卡") String sourceAccount,
            @JsonPropertyDescription("交易发生时间，ISO8601，默认当前时间") OffsetDateTime occurredAt
    ) {
    }

    @Bean
    @Description("在 Firefly III 中创建一笔新的支出记录")
    public Function<TransactionRequest, String> createExpense(FireflyApiClient apiClient,
                                                              FinanceCategoryMapper mapper) {
        return request -> {
            OffsetDateTime happenedAt = Optional.ofNullable(request.occurredAt())
                    .orElseGet(OffsetDateTime::now);
            FireflyExpenseCommand command = FireflyExpenseCommand.builder()
                    .amount(BigDecimal.valueOf(request.amount()))
                    .description(request.description())
                    .category(mapper.resolveCategory(request.category()))
                    .sourceAccount(mapper.resolveSourceAccount(request.sourceAccount()))
                    .destinationAccount(mapper.defaultDestinationAccount())
                    .occurredAt(happenedAt)
                    .currency(mapper.defaultCurrency())
                    .build();
            String transactionId = apiClient.createExpense(command);
            return "记账成功，交易 ID: " + transactionId;
        };
    }
}
