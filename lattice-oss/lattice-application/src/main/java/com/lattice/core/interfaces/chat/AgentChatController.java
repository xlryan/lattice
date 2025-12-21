package com.lattice.core.interfaces.chat;

import com.lattice.core.application.agent.AgentRequest;
import com.lattice.core.application.agent.AgentResponse;
import com.lattice.core.application.agent.LatticeAgentOrchestrator;
import com.lattice.core.application.chat.ChatHistoryService;
import com.lattice.core.domain.chat.ChatSession;
import com.lattice.core.infrastructure.logging.TraceContextHolder;
import com.lattice.core.infrastructure.tools.PythonWorkerClient;
import com.lattice.core.interfaces.shared.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Tag(name = "Chat")
@RestController
@RequestMapping("/api/chat")
public class AgentChatController {

    private static final Logger log = LoggerFactory.getLogger(AgentChatController.class);

    private final LatticeAgentOrchestrator orchestrator;
    private final ChatHistoryService chatHistoryService;

    public AgentChatController(LatticeAgentOrchestrator orchestrator, ChatHistoryService chatHistoryService) {
        this.orchestrator = orchestrator;
        this.chatHistoryService = chatHistoryService;
    }

    @Operation(summary = "Streaming agent conversation endpoint")
    @PostMapping(value = "/stream", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SseEmitter chat(@RequestPart("message") @NotBlank String message,
                           @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments,
                           @RequestParam(value = "sessionId", required = false) UUID sessionId,
                           Authentication authentication) throws IOException {
        // 0. 设置长超时时间 (0表示无限，或者设置 5分钟 = 300000L)
        SseEmitter emitter = new SseEmitter(0L);

        // 1. 主线程：预处理请求参数
        List<PythonWorkerClient.ImagePayload> payloads = toPayloads(attachments);
        String username = authentication == null ? "anonymous" : authentication.getName();
        AgentRequest request = new AgentRequest(message, payloads, username);

        // 🌟 关键修复：捕获当前主线程的 TraceId
        String traceId = TraceContextHolder.currentTraceId();

        log.info("[TraceId={}] 收到聊天请求 - 用户: {}, SessionId: {}, 附件数: {}",
                traceId, username, sessionId, payloads.size());

        // 2. 异步执行：防止阻塞 Web 容器线程
        CompletableFuture.runAsync(() -> {
            // 🌟 关键修复：在子线程中恢复 TraceId，确保日志链路完整
            TraceContextHolder.setTraceId(traceId);

            try {
                log.info("异步处理开始 - 正在初始化会话...");

                // 🌟 立即发送心跳，防止前端 fetch-event-source 因长时间无数据而重试
                emitter.send(SseEmitter.event().name("message").data("..."));

                // 3. 会话管理 (Session Management)
                UUID finalSessionId;
                if (sessionId == null) {
                    // 自动生成标题 (截取前20个字)
                    String title = message.length() > 20 ? message.substring(0, 20) + "..." : message;
                    ChatSession session = chatHistoryService.createSession(username, title);
                    finalSessionId = session.getId();
                    log.info("创建新会话成功 - SessionId: {}", finalSessionId);
                } else {
                    finalSessionId = sessionId;
                }

                // 4. 持久化用户消息
                chatHistoryService.saveMessage(finalSessionId, "user", message);

                // 5. 执行 Agent 核心逻辑 (Orchestrator)
                log.info("正在调度 Agent 执行业务逻辑...");
                long start = System.currentTimeMillis();
                AgentResponse response = orchestrator.handle(request);
                long duration = System.currentTimeMillis() - start;
                log.info("Agent 执行完成 - 耗时: {}ms, 意图: {}", duration, response.intent());

                // 6. 持久化 AI 响应
                chatHistoryService.saveMessage(finalSessionId, "assistant", response.message());

                // 7. 构造回包 (带上 SessionId 以便前端下次复用)
                // 注意：这里假设 AgentResponse 有一个对应的构造函数或你是用 record 允许这种构造
                // 如果没有，你需要新建一个 DTO 或修改 AgentResponse 结构
                AgentResponse responseWithSession = new AgentResponse(
                        response.intent(),
                        response.message(),
                        finalSessionId // 确保将 session id 返回给前端
                );

                // 8. 发送 SSE 响应
                emitter.send(SseEmitter.event()
                        .data(ApiResponse.success(responseWithSession), MediaType.APPLICATION_JSON));

                // 结束流
                emitter.complete();
                log.info("SSE 响应推送成功，流已关闭");

            } catch (Exception ex) {
                log.error("聊天流处理发生异常", ex);
                try {
                    // 尝试给前端发送一个错误事件，而不是直接断开，体验更好
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(ApiResponse.failure("INTERNAL_ERROR", "处理失败: " + ex.getMessage(), traceId), MediaType.APPLICATION_JSON));
                } catch (IOException ignored) {
                    // 客户端可能已经断开连接
                }
                emitter.completeWithError(ex);
            } finally {
                // 🌟 关键修复：清理子线程的 ThreadLocal，防止线程池污染
                TraceContextHolder.clear();
            }
        });

        return emitter;
    }

    private List<PythonWorkerClient.ImagePayload> toPayloads(List<MultipartFile> attachments) throws IOException {
        if (attachments == null || attachments.isEmpty()) {
            return List.of();
        }
        List<PythonWorkerClient.ImagePayload> payloads = new ArrayList<>();
        for (MultipartFile file : attachments) {
            payloads.add(new PythonWorkerClient.ImagePayload(file.getBytes(), file.getOriginalFilename(), file.getContentType()));
        }
        return payloads;
    }
}