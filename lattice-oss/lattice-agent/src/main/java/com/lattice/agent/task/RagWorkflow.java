package com.lattice.agent.task;

import com.lattice.core.domain.DomainType;
import com.lattice.retrieval.api.SearchRequest;
import com.lattice.retrieval.service.RetrievalService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 简单的 RAG 工作流，先检索再调用 LLM 汇总回答。
 */
@Component
@RequiredArgsConstructor
public class RagWorkflow {

    private final RetrievalService retrievalService;
    private final ChatModel chatModel;

    public String answer(String question, DomainType domainType) {
        var response = retrievalService.search(new SearchRequest(question, domainType, null, 5, 0.5d));
        String context = response.results().stream()
                .map(hit -> "- " + hit.title() + ": " + hit.snippet())
                .collect(Collectors.joining("\n"));
        PromptTemplate template = new PromptTemplate("""
                结合以下资料回答用户问题，若无答案请明确告知。\n
                背景:\n{context}\n
                问题: {question}
                """);
        return chatModel.call(template.create(Map.of(
                "context", context,
                "question", question
        ))).getResult().getOutput().getText();
    }
}
