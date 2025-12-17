package com.lattice.core.application.career;

import com.lattice.core.application.career.dto.StarRecord;
import com.lattice.core.domain.career.CareerNode;
import com.lattice.core.domain.career.CareerType;
import com.lattice.core.infrastructure.observability.AiUsageMonitor;
import com.lattice.core.infrastructure.prompt.PromptRegistry;
import com.lattice.core.repository.career.CareerRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.ChatClientResponse.ChatClientResult;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = CareerService.class)
@Import(BeanOutputConverter.class)
class CareerServiceTest {

    @MockBean
    CareerRepository repository;
    @MockBean
    ChatClient chatClient;
    @MockBean
    EmbeddingClient embeddingClient;
    @MockBean
    PromptRegistry promptRegistry;
    @MockBean
    AiUsageMonitor aiUsageMonitor;

    @Test
    void shouldPersistStarRecordFromAi() {
        when(promptRegistry.resolveContent("career.star")).thenReturn("prompt");
        ChatClientResponse response = ChatClientResponse.builder()
                .withResult(new ChatClientResult(List.of(new Generation("{\"situation\":\"S\",\"task\":\"T\",\"action\":\"A\",\"result\":\"R\"}")), Map.of(), null))
                .build();
        when(chatClient.prompt(any(Prompt.class))).thenReturn(builder -> response);
        when(embeddingClient.embed(any(String.class))).thenReturn(List.of(0.1, 0.2));

        CareerService service = new CareerService(repository, chatClient, embeddingClient, promptRegistry, aiUsageMonitor);
        service.createLog("raw", CareerType.WORK_LOG, List.of("tag"));
        Mockito.verify(repository).save(any(CareerNode.class));
    }
}
