package com.lattice.agent.finance.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Firefly III 创建交易的请求载荷。
 */
public record FireflyTransactionRequest(List<Transaction> transactions) {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public static FireflyTransactionRequest fromCommand(FireflyExpenseCommand command) {
        Transaction payload = new Transaction(
                "withdrawal",
                FORMATTER.format(command.occurredAt()),
                command.amount().toPlainString(),
                command.description(),
                command.sourceAccount(),
                command.destinationAccount(),
                command.category(),
                command.currency()
        );
        return new FireflyTransactionRequest(List.of(payload));
    }

    public record Transaction(
            String type,
            String date,
            String amount,
            String description,
            @JsonProperty("source_name") String sourceName,
            @JsonProperty("destination_name") String destinationName,
            @JsonProperty("category_name") String categoryName,
            @JsonProperty("currency_code") String currencyCode
    ) {
    }
}
