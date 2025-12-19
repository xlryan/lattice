package com.lattice.core.infrastructure.tools;

import com.lattice.agent.config.FireflyProperties;
import com.lattice.agent.finance.model.FireflyExpenseCommand;
import com.lattice.core.infrastructure.exception.ExternalServiceException;
import com.lattice.core.infrastructure.logging.TraceContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Firefly III REST adapter for wealth tooling.
 */
@Component
public class FireflyClient {

    private static final Logger log = LoggerFactory.getLogger(FireflyClient.class);

    private final RestClient restClient;

    public FireflyClient(RestClient.Builder builder, FireflyProperties properties) {
        this.restClient = builder
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getToken())
                .build();
    }

    public FireflyTransactionResult createExpense(FireflyExpenseCommand command) {
        try {
            log.info("[traceId={}] Posting expense to Firefly for {} {}", TraceContextHolder.currentTraceId(),
                    command.amount(), command.description());
            FireflyTransactionRequest payload = FireflyTransactionRequest.from(command);
            FireflyTransactionResponse response = restClient.post()
                    .uri("/api/v1/transactions")
                    .body(payload)
                    .retrieve()
                    .body(FireflyTransactionResponse.class);
            return new FireflyTransactionResult(response.transactionIds());
        } catch (RestClientException ex) {
            throw new ExternalServiceException("Firefly expense creation failed", ex);
        }
    }

    public BalanceSnapshot fetchBalances() {
        try {
            log.info("[traceId={}] Fetching Firefly balance summary", TraceContextHolder.currentTraceId());
            FireflyBalanceResponse response = restClient.get()
                    .uri("/api/v1/accounts?type=asset")
                    .retrieve()
                    .body(FireflyBalanceResponse.class);
            return new BalanceSnapshot(response == null ? List.of() : response.toEntries());
        } catch (RestClientException ex) {
            throw new ExternalServiceException("Firefly balance query failed", ex);
        }
    }

    public List<FireflyTransactionItem> fetchTransactions() {
        try {
            log.info("[traceId={}] Fetching Firefly transactions", TraceContextHolder.currentTraceId());
            TransactionsResponse response = restClient.get()
                    .uri("/api/v1/transactions")
                    .retrieve()
                    .body(TransactionsResponse.class);
            return response == null ? List.of() : response.toItems();
        } catch (RestClientException ex) {
            throw new ExternalServiceException("Firefly transactions query failed", ex);
        }
    }

    private record FireflyTransactionRequest(TransactionWrapper transactions) {
        static FireflyTransactionRequest from(FireflyExpenseCommand command) {
            Transaction transaction = new Transaction(
                    "withdrawal",
                    command.amount().toPlainString(),
                    command.description(),
                    command.category(),
                    command.sourceAccount(),
                    command.destinationAccount(),
                    command.currency(),
                    command.occurredAt().toString()
            );
            return new FireflyTransactionRequest(new TransactionWrapper(List.of(transaction)));
        }
    }

    private record TransactionWrapper(List<Transaction> transactions) {
    }

    private record Transaction(String type,
                               String amount,
                               String description,
                               String category_name,
                               String source_name,
                               String destination_name,
                               String currency_code,
                               String date) {
    }

    private record FireflyTransactionResponse(FireflyData data) {
        List<String> transactionIds() {
            if (data == null || data.attributes == null) {
                return List.of();
            }
            return data.attributes.transactions.stream()
                    .map(tx -> tx.transaction_journal_id)
                    .toList();
        }
    }

    private record FireflyData(String id, FireflyAttributes attributes) {
    }

    private record FireflyAttributes(List<FireflyTransaction> transactions) {
    }

    private record FireflyTransaction(String transaction_journal_id) {
    }

    private record FireflyBalanceResponse(List<AccountData> data) {
        List<BalanceEntry> toEntries() {
            if (data == null) {
                return List.of();
            }
            return data.stream()
                    .map(AccountData::toBalanceEntry)
                    .toList();
        }
    }

    private record AccountData(String id, AccountAttributes attributes) {
        BalanceEntry toBalanceEntry() {
            BigDecimal balance = attributes.current_balance == null ? BigDecimal.ZERO : attributes.current_balance;
            return new BalanceEntry(attributes.name, balance, attributes.currency_code);
        }
    }

    private record AccountAttributes(String name, BigDecimal current_balance, String currency_code) {
    }

    public record FireflyTransactionResult(List<String> journalIds) {
    }

    public record BalanceSnapshot(List<BalanceEntry> entries) {
    }

    public record BalanceEntry(String accountName, BigDecimal balance, String currencyCode) {
    }

    private record TransactionsResponse(List<TransactionData> data) {
        List<FireflyTransactionItem> toItems() {
            if (data == null) return List.of();
            return data.stream()
                    .flatMap(d -> d.attributes.transactions.stream()
                            .map(t -> new FireflyTransactionItem(
                                    t.transaction_journal_id,
                                    t.date,
                                    t.description,
                                    new BigDecimal(t.amount),
                                    t.type,
                                    t.category_name,
                                    t.source_name
                            )))
                    .toList();
        }
    }

    private record TransactionData(TransactionAttributes attributes) {}
    private record TransactionAttributes(List<Transaction> transactions) {}

    public record FireflyTransactionItem(
            String id,
            String date,
            String description,
            BigDecimal amount,
            String type,
            String category,
            String account
    ) {}
}
