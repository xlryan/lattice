package com.lattice.agent.finance.client;

import com.lattice.agent.finance.model.FireflyTransactionRequest;
import com.lattice.agent.finance.model.FireflyTransactionResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * Spring 6 HTTP Interface，用于直接对接 Firefly III REST API。
 */
@HttpExchange(accept = MediaType.APPLICATION_JSON_VALUE, contentType = MediaType.APPLICATION_JSON_VALUE)
public interface FireflyHttpClient {

    @PostExchange("/api/v1/transactions")
    FireflyTransactionResponse createTransaction(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestBody FireflyTransactionRequest request);
}
