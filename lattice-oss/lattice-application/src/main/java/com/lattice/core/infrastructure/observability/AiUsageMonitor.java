package com.lattice.core.infrastructure.observability;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Records token usage for LLM calls to Prometheus via Micrometer.
 */
@Component
public class AiUsageMonitor {

    private final MeterRegistry meterRegistry;

    public AiUsageMonitor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void record(String model, String domain, long promptTokens, long completionTokens) {
        String safeModel = StringUtils.hasText(model) ? model : "unknown";
        String safeDomain = StringUtils.hasText(domain) ? domain : "GENERIC";
        if (promptTokens > 0) {
            meterRegistry.counter("ai.token.usage", "model", safeModel, "domain", safeDomain, "type", "prompt")
                    .increment(promptTokens);
        }
        if (completionTokens > 0) {
            meterRegistry.counter("ai.token.usage", "model", safeModel, "domain", safeDomain, "type", "completion")
                    .increment(completionTokens);
        }
    }
}
