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
        log.info("[traceId={}] Orchestrator received request for user: {}", TraceContextHolder.currentTraceId(), request.userId());
        log.debug("[traceId={}] Request content: {}", TraceContextHolder.currentTraceId(), request.userInput());
        
        long startTime = System.currentTimeMillis();
        // 调用底层的 AgentService 执行推理与工具调用
        String result = agentService.execute(request.userInput());
        long duration = System.currentTimeMillis() - startTime;
        
        log.info("[traceId={}] Agent execution completed in {} ms", TraceContextHolder.currentTraceId(), duration);
        log.debug("[traceId={}] Agent result: {}", TraceContextHolder.currentTraceId(), result);
        
        // 目前简单封装为 CHAT 意图返回给前端，后续可根据结果类型动态调整
        return new AgentResponse(AgentIntent.CHAT, result, null);
    }
}

