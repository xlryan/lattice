package com.lattice.core.application.agent;

import com.lattice.core.infrastructure.tools.PythonWorkerClient;

import java.util.List;

/**
 * Canonical agent request containing text and optional image payloads.
 */
public record AgentRequest(String userInput,
                           List<PythonWorkerClient.ImagePayload> images,
                           String userId) {
}
