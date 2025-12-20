package com.lattice.core.repository;

import com.lattice.core.domain.chat.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {
    List<ChatSession> findByUser_UsernameOrderByUpdatedAtDesc(String username);
}
