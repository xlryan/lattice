package com.lattice.agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Firefly III API 访问配置，集中管理 baseUrl、token 以及默认账户映射。
 */
@ConfigurationProperties(prefix = "lattice.finance.firefly")
public class FireflyProperties {

    private String baseUrl;
    private String token;
    private String defaultSourceAccount = "Cash";
    private String defaultDestinationAccount = "Expenses";
    private String defaultCategory = "Misc";
    private String currency = "CNY";
    private Map<String, String> categoryMappings = new HashMap<>();

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDefaultSourceAccount() {
        return defaultSourceAccount;
    }

    public void setDefaultSourceAccount(String defaultSourceAccount) {
        this.defaultSourceAccount = defaultSourceAccount;
    }

    public String getDefaultDestinationAccount() {
        return defaultDestinationAccount;
    }

    public void setDefaultDestinationAccount(String defaultDestinationAccount) {
        this.defaultDestinationAccount = defaultDestinationAccount;
    }

    public String getDefaultCategory() {
        return defaultCategory;
    }

    public void setDefaultCategory(String defaultCategory) {
        this.defaultCategory = defaultCategory;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Map<String, String> getCategoryMappings() {
        return categoryMappings;
    }

    public void setCategoryMappings(Map<String, String> categoryMappings) {
        this.categoryMappings = categoryMappings;
    }
}
