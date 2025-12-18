package com.lattice.agent.finance.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Firefly III 返回的交易结果，提取 transaction_journal_id 便于展示。
 */
public record FireflyTransactionResponse(@JsonProperty("data") Data data) {

    public Optional<String> firstJournalId() {
        if (data == null || data.attributes == null) {
            return Optional.empty();
        }
        List<Transaction> transactions = data.attributes.transactions;
        if (transactions == null || transactions.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(transactions.get(0).transactionJournalId);
    }

    public record Data(String id, Attributes attributes) {
    }

    public record Attributes(@JsonProperty("transactions") List<Transaction> transactions) {
        public Attributes {
            transactions = transactions == null ? Collections.emptyList() : transactions;
        }
    }

    public record Transaction(@JsonProperty("transaction_journal_id") String transactionJournalId) {
    }
}
