package com.lattice.agent.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LatticeAgentServiceTest {

    @Mock
    private ChatModel chatModel;

    @InjectMocks
    private LatticeAgentService agentService;

    @Test
    void testExecuteTriggersToolCallingConfig() {
        // 准备 Mock 响应
        Generation generation = new Generation("我已为您完成记账。");
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // 执行调用
        String query = "记一笔 50 块的咖啡账单";
        String result = agentService.execute(query);

        // 验证结果
        assertThat(result).isEqualTo("我已为您完成记账。");

        // 核心验证：验证发送给 LLM 的 Prompt 是否包含了预期的工具函数
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());
        
        Prompt capturedPrompt = promptCaptor.getValue();
        assertThat(capturedPrompt.getOptions()).isInstanceOf(OpenAiChatOptions.class);
        
        OpenAiChatOptions options = (OpenAiChatOptions) capturedPrompt.getOptions();
        assertThat(options.getFunctions()).contains("createExpense", "searchLattice");
        
        // 验证提示词内容
        String fullPrompt = capturedPrompt.getInstructions().get(0).getContent();
        assertThat(fullPrompt).contains("Lattice");
        assertThat(fullPrompt).contains("searchLattice");
    }
}
