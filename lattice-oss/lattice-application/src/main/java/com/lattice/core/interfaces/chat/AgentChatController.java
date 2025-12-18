package com.lattice.core.interfaces.chat;

import com.lattice.core.application.agent.AgentRequest;
import com.lattice.core.application.agent.AgentResponse;
import com.lattice.core.application.agent.LatticeAgentOrchestrator;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Tag(name = "Chat")
@RestController
@RequestMapping("/api/chat")
public class AgentChatController {

    private static final Logger log = LoggerFactory.getLogger(AgentChatController.class);

    private final LatticeAgentOrchestrator orchestrator;

    public AgentChatController(LatticeAgentOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @Operation(summary = "Streaming agent conversation endpoint")
    @PostMapping(value = "/stream", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SseEmitter chat(@RequestPart("message") @NotBlank String message,
                           @RequestPart(value = "images", required = false) List<MultipartFile> images,
                           Authentication authentication) throws IOException {
        SseEmitter emitter = new SseEmitter(0L);
        List<PythonWorkerClient.ImagePayload> payloads = toPayloads(images);
        AgentRequest request = new AgentRequest(message, payloads, authentication == null ? null : authentication.getName());
        CompletableFuture.runAsync(() -> {
            try {
                AgentResponse response = orchestrator.handle(request);
                emitter.send(SseEmitter.event().id("intent").data(ApiResponse.success(response)));
                emitter.complete();
            } catch (Exception ex) {
                log.error("[traceId={}] Chat stream error", TraceContextHolder.currentTraceId(), ex);
                try {
                    emitter.send(SseEmitter.event().id("error").data(ApiResponse.failure("ERROR", ex.getMessage(), TraceContextHolder.currentTraceId())));
                } catch (IOException ignored) {
                }
                emitter.completeWithError(ex);
            }
        });
        return emitter;
    }

    private List<PythonWorkerClient.ImagePayload> toPayloads(List<MultipartFile> images) throws IOException {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        List<PythonWorkerClient.ImagePayload> payloads = new ArrayList<>();
        for (MultipartFile file : images) {
            payloads.add(new PythonWorkerClient.ImagePayload(file.getBytes(), file.getOriginalFilename(), file.getContentType()));
        }
        return payloads;
    }
}
