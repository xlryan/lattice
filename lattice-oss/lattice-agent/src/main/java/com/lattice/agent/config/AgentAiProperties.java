package com.lattice.agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI 供应商配置，从 application.yml 中读取，以方便切换 DeepSeek/Ollama。
 */
@ConfigurationProperties(prefix = "lattice.ai")
public class AgentAiProperties {

    private ProviderProperties classification = new ProviderProperties();
    private ProviderProperties embedding = new ProviderProperties();

    public ProviderProperties getClassification() {
        return classification;
    }

    public void setClassification(ProviderProperties classification) {
        this.classification = classification;
    }

    public ProviderProperties getEmbedding() {
        return embedding;
    }

    public void setEmbedding(ProviderProperties embedding) {
        this.embedding = embedding;
    }

    public static class ProviderProperties {
        private String baseUrl;
        private String model;
        private String apiKey;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }
}
