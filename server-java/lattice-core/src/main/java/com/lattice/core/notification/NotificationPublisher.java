package com.lattice.core.notification;

/**
 * 发布节点事件的统一接口，具体实现可对接 ntfy、Webhook 等。
 */
public interface NotificationPublisher {

    void publish(NodeNotification notification);
}
