package com.lattice.agent.finance.service;

import com.lattice.agent.config.FireflyProperties;
import com.lattice.agent.finance.client.FireflyHttpClient;
import com.lattice.agent.finance.model.FireflyExpenseCommand;
import com.lattice.agent.finance.model.FireflyTransactionRequest;
import com.lattice.agent.finance.model.FireflyTransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 封装 Firefly HTTP 调用，供工具函数复用。
 */
@Service
@RequiredArgsConstructor
public class FireflyApiClient {

    private final FireflyHttpClient httpClient;
    private final FireflyProperties properties;

    public String createExpense(FireflyExpenseCommand command) {
        FireflyTransactionRequest request = FireflyTransactionRequest.fromCommand(command);
        FireflyTransactionResponse response = httpClient.createTransaction(bearerToken(), request);
        return response.firstJournalId()
                .orElseGet(() -> response.data() != null ? response.data().id() : "unknown");
    }

    private String bearerToken() {
        return "Bearer " + properties.getToken();
    }
}
