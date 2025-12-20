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
import org.springframework.ai.ollama.api.OllamaChatOptions;
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
        log.info("Lattice Agent executing query: {}", query);

        SystemMessage systemMessage = new SystemMessage("""
                你是一个名为 Lattice (晶格) 的个人助手。
                
                你具备以下能力：
                1. 'createExpense': 当用户提到要记账或记录支出时调用。
                2. 'searchLattice': 当用户询问关于他们生活记录、职业、DIY 项目等库中已有的信息时调用。
                3. 'saveNote': 当用户想要记录普通的笔记、想法、日记、或者任何非财务类的记录时调用。
                
                交互规范：
                - 如果用户的输入是操作指令（如下单、记账、存笔记），请务必调用相应的工具。
                - 在工具调用返回结果后，请根据工具返回的内容，向用户发送一个友好的确认消息。
                - 所有的回复请使用中文。
                - 不要返回类似 "System: Please continue" 之类的调试信息。
                """);

        UserMessage userMessage = new UserMessage(query);

        // 注册可用的函数列表
        OllamaChatOptions options = OllamaChatOptions.builder()
                .toolNames(java.util.Set.of("createExpense", "searchLattice", "saveNote"))
                .build();

        log.debug("Constructed prompt with tools: {}", options.getToolNames());

        ChatResponse response = chatModel.call(new Prompt(List.of(systemMessage, userMessage), options));
        
        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
            log.error("Received null response from ChatModel");
            return "抱歉，我现在无法处理您的请求。";
        }

        String output = response.getResult().getOutput().getText();
        log.info("Final AI Output: {}", output);
        log.debug("LLM Usage: {}", response.getMetadata().getUsage());
        
        return output;
    }
}
