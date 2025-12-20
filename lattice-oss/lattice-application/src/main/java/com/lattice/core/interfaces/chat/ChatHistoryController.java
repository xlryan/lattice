package com.lattice.core.interfaces.chat;

import com.lattice.core.application.chat.ChatHistoryService;
import com.lattice.core.domain.chat.ChatMessage;
import com.lattice.core.domain.chat.ChatSession;
import com.lattice.core.interfaces.shared.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Chat History")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatHistoryController {

    private final ChatHistoryService chatHistoryService;

    @Operation(summary = "List user chat sessions")
    @GetMapping("/sessions")
    public ApiResponse<List<ChatSession>> getSessions(Authentication authentication) {
        String username = authentication.getName();
        List<ChatSession> sessions = chatHistoryService.getUserSessions(username);
        // 显式清理，防止 Jackson 尝试序列化延迟加载的 User 对象
        sessions.forEach(s -> s.setUser(null));
        return ApiResponse.success(sessions);
    }

    @Operation(summary = "List messages in a session")
    @GetMapping("/sessions/{sessionId}/messages")
    public ApiResponse<List<ChatMessage>> getSessionMessages(@PathVariable UUID sessionId) {
        List<ChatMessage> messages = chatHistoryService.getSessionMessages(sessionId);
        // 显式清理，防止 Jackson 尝试序列化延迟加载的 Session 对象
        messages.forEach(m -> m.setSession(null));
        return ApiResponse.success(messages);
    }
}
