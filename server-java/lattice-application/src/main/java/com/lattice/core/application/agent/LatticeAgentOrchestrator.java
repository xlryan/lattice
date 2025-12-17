package com.lattice.core.application.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lattice.core.application.career.CareerService;
import com.lattice.core.application.wealth.WealthService;
import com.lattice.core.application.wealth.WealthService.ExpenseCommand;
import com.lattice.core.domain.career.CareerType;
import com.lattice.core.infrastructure.logging.TraceContextHolder;
import com.lattice.core.infrastructure.tools.PythonWorkerClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Router + Specialist orchestrator coordinating LLM intent decisions and domain services.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LatticeAgentOrchestrator {

    private static final String INTENT_PROMPT = """
            你是路由器，请从 CAREER/WEALTH/KNOWLEDGE/CHAT 中选择最合适的意图，输出 JSON。
            示例: {"intent":"WEALTH","reason":"需要记账"}
            文本: {input}
            """;

    private final ChatClient chatClient;
    private final WealthService wealthService;
    private final CareerService careerService;
    private final ObjectMapper objectMapper;

    @Transactional
    public AgentResponse handle(AgentRequest request) {
        log.info("[traceId={}] Orchestrator received request userId={}", TraceContextHolder.currentTraceId(), request.userId());
        AgentIntent intent = classifyIntent(request.userInput());
        log.info("[traceId={}] Classified intent={}", TraceContextHolder.currentTraceId(), intent);
        return switch (intent) {
            case WEALTH -> handleWealth(request);
            case CAREER -> handleCareer(request);
            case KNOWLEDGE -> handleKnowledge(request);
            case CHAT -> handleChat(request);
        };
    }

    private AgentIntent classifyIntent(String input) {
        try {
            String response = chatClient.call(new PromptTemplate(INTENT_PROMPT).create(Map.of("input", input)))
                    .getResult().getOutput().getContent();
            IntentResult intentResult = objectMapper.readValue(response, IntentResult.class);
            return AgentIntent.valueOf(intentResult.intent());
        } catch (Exception ex) {
            log.warn("[traceId={}] Intent classification fallback", TraceContextHolder.currentTraceId(), ex);
            return AgentIntent.CHAT;
        }
    }

    private AgentResponse handleWealth(AgentRequest request) {
        List<PythonWorkerClient.ImagePayload> images = request.images() == null ? List.of() : request.images();
        PythonWorkerClient.ImagePayload payload = images.isEmpty() ? null : images.getFirst();
        ExpenseCommand command = new ExpenseCommand(
                request.userInput(),
                "General",
                null,
                null,
                null,
                OffsetDateTime.now(),
                "agent",
                request.images() == null ? List.of() : List.of("image-upload"),
                payload
        );
        wealthService.ingestExpense(command);
        return new AgentResponse(AgentIntent.WEALTH, "已完成记账，并同步 Firefly");
    }

    private AgentResponse handleCareer(AgentRequest request) {
        careerService.createLog(request.userInput(), CareerType.WORK_LOG, List.of());
        return new AgentResponse(AgentIntent.CAREER, "已写入职业日志，完成 STAR 结构化");
    }

    private AgentResponse handleKnowledge(AgentRequest request) {
        // placeholder for RAG retrieval; for now, echo message
        return new AgentResponse(AgentIntent.KNOWLEDGE, "知识库功能开发中，原文：" + request.userInput());
    }

    private AgentResponse handleChat(AgentRequest request) {
        String reply = chatClient.prompt(builder -> builder.withUser(request.userInput()))
                .call().getResult().getOutput().getContent();
        return new AgentResponse(AgentIntent.CHAT, reply);
    }

    private record IntentResult(String intent, String reason) {
    }
}
