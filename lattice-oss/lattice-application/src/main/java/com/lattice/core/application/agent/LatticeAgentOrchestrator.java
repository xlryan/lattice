package com.lattice.core.application.agent;

import com.lattice.agent.task.LatticeAgentService;
import com.lattice.core.infrastructure.logging.TraceContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Orchestrator leveraging Spring AI Tool-Calling for intelligent task execution.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LatticeAgentOrchestrator {

    private final LatticeAgentService agentService;

    public AgentResponse handle(AgentRequest request) {
        log.info("[traceId={}] Orchestrator delegating to LatticeAgentService for input: {}", 
                TraceContextHolder.currentTraceId(), request.userInput());
        
        // 调用底层的 AgentService 执行推理与工具调用
        String result = agentService.execute(request.userInput());
        
        // 目前简单封装为 CHAT 意图返回给前端，后续可根据结果类型动态调整
        return new AgentResponse(AgentIntent.CHAT, result);
    }
}

