package com.lattice.agent.task;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Lattice 核心执行代理 (Core Execution Agent).
 * 负责上下文组装、Prompt 工程以及工具调用的编排。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LatticeAgentService {

    private final ChatModel chatModel;

    // 将 System Prompt 提取为常量，方便维护或从配置中心加载
    private static final String SYSTEM_PROMPT = """
            # Role
            你是 Lattice (晶格) 系统的首席数据官。你的使命是协助用户对抗生活中的“熵增”，将碎片化的信息转化为结构化、高价值的数据资产。
            
            # Core Philosophy (核心原则)
            1. **结构化优先**：不要只存储文本。尽最大努力从自然语言中提取：时间、金额、标签、相关人员、技术栈等元数据。
            2. **最小阻力**：用户通常很懒。如果用户输入模糊（如“买了个鼠标”），先调用工具记录已知信息（如 Item="鼠标"），利用工具返回的“缺省/待确认”状态，再自然地询问缺失信息（如“多少钱？”）。不要让用户感到被“审问”。
            3. **领域感知**：
               - 财富 (Wealth): 关注金额、商户、分类（餐饮/交通等）。
               - 知识 (Knowledge): 关注标签、核心观点、来源。
               - 职业 (Career): 关注技能点 (STAR法则)、项目名。
            
            # Tool Usage Guidelines (工具使用指南)
            你拥有以下神兵利器 (Tools)：
            
            ### 1. createExpense (记账)
            - **触发**: 用户涉及消费、收入、转账。
            - **策略**: 即使金额缺失，也应先尝试调用。如果用户后续补充了金额，视为对上一条记录的修正（系统会自动处理上下文）。
            - **示例**:\s
              - User: "打车去公司" -> Call createExpense(desc="打车", category="交通") -> AI: "记下来了。这趟行程花了多少钱？"
              - User: "35元" -> Call createExpense(amount=35, desc="打车-补充") -> AI: "已更新金额为 35元。"
            
            ### 2. saveNote (记录/灵感)
            - **触发**: 日记、备忘、灵感、代码片段、待办。
            - **策略**: 自动生成 2-3 个标签 (Tags)。如果内容是技术相关的，自动归类到 Career 领域。
            - **示例**:
              - User: "今天学会了 Spring AI 的 Function Calling" -> Call saveNote(content="...", tags=["Spring AI", "Java", "Learning"])
            
            ### 3. searchLattice (第二大脑检索)
            - **触发**: 用户问“我以前...”、“记得...吗”、“查一下...”。
            - **策略**: 先检索，再基于检索结果回答。如果没搜到，诚实地告诉用户并建议记录下来。
            
            # Response Style (回复风格)
            - **专业且有温度**：不要像机器人一样机械复述。
            - **简练**：确认操作时，用“✅ 已记录”、“👌 搞定”等短语，然后跟进关键信息。
            - **Markdown**：使用加粗强调关键数据（如 **¥25.00**），使用列表展示检索结果。
            
            现在，请作为 Lattice 数据官，开始处理用户的下一条指令。
            """;

    /**
     * 执行代理任务 (无历史上下文版 - 不推荐用于多轮对话)
     */
    public String execute(String query) {
        return execute(query, List.of());
    }

    /**
     * 执行代理任务 (支持多轮对话上下文)
     *
     * @param query          用户当前问题
     * @param historyMessages 历史对话记录 (User + Assistant)
     * @return AI 的响应
     */
    public String execute(String query, List<Message> historyMessages) {
        log.info("Lattice Agent executing query: [{}], History size: {}", query, historyMessages.size());

        // 1. 构建 Prompt 上下文堆栈
        // 顺序: System Prompt -> History (User/AI/User/AI) -> Current User Message
        List<Message> promptMessages = new ArrayList<>();
        promptMessages.add(new SystemMessage(SYSTEM_PROMPT));

        if (historyMessages != null && !historyMessages.isEmpty()) {
            // 注意：这里需要控制历史记录的长度（Token Window），防止爆 Token
            // 简单策略：取最近 10 条
            int start = Math.max(0, historyMessages.size() - 10);
            promptMessages.addAll(historyMessages.subList(start, historyMessages.size()));
        }

        promptMessages.add(new UserMessage(query));

        // 2. 配置工具 (Function Calling)
        // 注意：这里假设你的 Tool @Bean 已经注册到了 Spring Context，并且名字匹配
        OllamaChatOptions options = OllamaChatOptions.builder()
                .toolNames(Set.of("createExpense", "searchLattice", "saveNote"))
                // .withTemperature(0.3f) // 降低温度，让工具调用更精准
                .build();

        // 3. 调用 LLM
        try {
            ChatResponse response = chatModel.call(new Prompt(promptMessages, options));

            if (!isValidResponse(response)) {
                log.warn("LLM returned empty response");
                return "Lattice 正在整理数据，请稍后再试... (Empty Response)";
            }

            String output = response.getResult().getOutput().getText();

            // 记录 Token 使用情况，用于成本监控
            if (response.getMetadata() != null && response.getMetadata().getUsage() != null) {
                log.debug("Token Usage: {}", response.getMetadata().getUsage());
            }

            log.info("Agent Output: {}", output);
            return output;

        } catch (Exception e) {
            log.error("Agent execution failed", e);
            // 优雅降级：当大模型挂了，至少回复用户
            return "我的思维链接似乎断开了，请检查后台日志。错误信息: " + e.getMessage();
        }
    }

    private boolean isValidResponse(ChatResponse response) {
        return response != null && response.getResult() != null && response.getResult().getOutput() != null;
    }
}