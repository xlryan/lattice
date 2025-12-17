package com.lattice.agent.notification;

import com.lattice.agent.config.NtfyProperties;
import com.lattice.core.notification.NodeNotification;
import com.lattice.core.notification.NotificationPublisher;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * 将节点事件推送到自托管的 ntfy server。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "lattice.notifications.ntfy", name = "enabled", havingValue = "true", matchIfMissing = true)
public class NtfyNotificationPublisher implements NotificationPublisher {

    private final RestClient.Builder restClientBuilder;
    private final NtfyProperties properties;
    private RestClient restClient;

    @PostConstruct
    void init() {
        this.restClient = restClientBuilder
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    @Override
    public void publish(NodeNotification notification) {
        try {
            String body = buildBody(notification);
            RestClient.RequestBodySpec request = restClient.post().uri("/" + properties.getTopic());
            request.header("Title", properties.getTitle());
            request.header("Priority", String.valueOf(properties.getPriority()));
            if (StringUtils.hasText(properties.getToken())) {
                request.header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getToken());
            }
            request.body(body).retrieve().toBodilessEntity();
        } catch (Exception ex) {
            log.warn("发送 ntfy 通知失败", ex);
        }
    }

    private String buildBody(NodeNotification notification) {
        String timestamp = DateTimeFormatter.ISO_INSTANT.format(notification.createdAt());
        return String.format(Locale.CHINA,
                "[%s] %s\n%s",
                notification.domain(),
                notification.title(),
                notification.preview() + "\n" + timestamp
        );
    }
}
