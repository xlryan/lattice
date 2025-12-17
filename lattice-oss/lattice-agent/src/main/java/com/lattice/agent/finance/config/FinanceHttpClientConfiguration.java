package com.lattice.agent.finance.config;

import com.lattice.agent.config.FireflyProperties;
import com.lattice.agent.finance.client.FireflyHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * 构建 Firefly HTTP Interface 对象，继承 Spring Boot RestClient 配置。
 */
@Configuration
public class FinanceHttpClientConfiguration {

    @Bean
    public FireflyHttpClient fireflyHttpClient(FireflyProperties properties, RestClient.Builder builder) {
        RestClient restClient = builder
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();
        return factory.createClient(FireflyHttpClient.class);
    }
}
