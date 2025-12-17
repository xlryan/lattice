package com.lattice.core.application.agent;

/**
 * Unified response emitted by the orchestrator.
 */
public record AgentResponse(AgentIntent intent, String message) {
}
