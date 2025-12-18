package com.lattice.agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ntfy 推送配置。
 */
@ConfigurationProperties(prefix = "lattice.notifications.ntfy")
public class NtfyProperties {

    private boolean enabled = true;
    private String baseUrl = "http://ntfy";
    private String topic = "lattice-events";
    private String title = "Lattice Node";
    private int priority = 3;
    private String token;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
