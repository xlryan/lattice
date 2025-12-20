package com.lattice.core.application.chat;

import com.lattice.core.domain.auth.User;
import com.lattice.core.domain.chat.ChatMessage;
import com.lattice.core.domain.chat.ChatSession;
import com.lattice.core.repository.ChatMessageRepository;
import com.lattice.core.repository.ChatSessionRepository;
import com.lattice.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatHistoryService {

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatSession createSession(String username, String title) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        ChatSession session = ChatSession.builder()
                .user(user)
                .title(title == null ? "New Chat " + Instant.now() : title)
                .build();
        return sessionRepository.save(session);
    }

    @Transactional
    public ChatMessage saveMessage(UUID sessionId, String role, String content) {
        ChatSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        // Update session timestamp
        session.setUpdatedAt(Instant.now());
        sessionRepository.save(session);

        ChatMessage message = ChatMessage.builder()
                .session(session)
                .role(role)
                .content(content)
                .build();
        return messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public List<ChatSession> getUserSessions(String username) {
        return sessionRepository.findByUser_UsernameOrderByUpdatedAtDesc(username);
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> getSessionMessages(UUID sessionId) {
        return messageRepository.findBySession_IdOrderByCreatedAtAsc(sessionId);
    }
    
    @Transactional(readOnly = true)
    public Optional<ChatSession> getSession(UUID sessionId) {
        return sessionRepository.findById(sessionId);
    }
}
