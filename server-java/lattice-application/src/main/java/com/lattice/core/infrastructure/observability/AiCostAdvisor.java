package com.lattice.core.infrastructure.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatResponseEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listens to Spring AI chat responses and records token usage metrics.
 */
@Component
public class AiCostAdvisor {

    private static final Logger log = LoggerFactory.getLogger(AiCostAdvisor.class);

    private final AiUsageMonitor usageMonitor;

    public AiCostAdvisor(AiUsageMonitor usageMonitor) {
        this.usageMonitor = usageMonitor;
    }

    @EventListener
    public void onChatResponse(ChatResponseEvent event) {
        if (event.getResponse() == null || event.getResponse().getMetadata() == null) {
            return;
        }
        var metadata = event.getResponse().getMetadata();
        var usage = metadata.getUsage();
        if (usage != null) {
            usageMonitor.record(metadata.getModel(), event.getContext().get("domain"),
                    usage.getPromptTokens() == null ? 0 : usage.getPromptTokens(),
                    usage.getCompletionTokens() == null ? 0 : usage.getCompletionTokens());
            log.debug("Recorded AI usage model={} domain={} prompt={} completion={}",
                    metadata.getModel(), event.getContext().get("domain"), usage.getPromptTokens(), usage.getCompletionTokens());
        }
    }
}
