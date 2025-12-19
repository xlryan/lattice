package com.lattice.agent.task;

import com.lattice.core.domain.DomainType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Lattice 核心执行代理。
 * 它不仅能检索知识（RAG），还能根据意图调用本地工具（Tool Calling）。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LatticeAgentService {

    private final ChatModel chatModel;

    /**
     * 执行代理任务
     * @param query 用户问题
     * @return AI 的响应（可能包含执行结果）
     */
    public String execute(String query) {
        log.info("Lattice Agent receiving query: {}", query);

        SystemMessage systemMessage = new SystemMessage("""
                你是一个名为 Lattice (晶格) 的个人助手。
                
                你可以通过调用以下函数来辅助回答：
                1. 'createExpense': 当用户提到要记账或记录支出时调用。
                2. 'searchLattice': 当用户询问关于他们生活记录、职业、DIY 项目等库中已有的信息时调用。
                
                处理流程：
                - 优先判断是否需要执行操作（如记账）。
                - 如果问题涉及背景知识，请先使用 'searchLattice' 获取信息后再回答。
                - 回答必须简洁，且如果是执行了操作，请务必返回确认信息。
                """);

        UserMessage userMessage = new UserMessage(query);

        // 注册可用的函数列表
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .toolNames(java.util.Set.of("createExpense", "searchLattice"))
                .build();







        ChatResponse response = chatModel.call(new Prompt(List.of(systemMessage, userMessage), options));
        
        return response.getResult().getOutput().getText();
    }
}
