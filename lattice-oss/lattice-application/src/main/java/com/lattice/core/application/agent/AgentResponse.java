package com.lattice.core.application.agent;

import java.util.UUID;

/**
 * Unified response emitted by the orchestrator.
 */
public record AgentResponse(AgentIntent intent, String message, UUID sessionId) {
}
