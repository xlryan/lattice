package com.lattice.core.application.career;

import com.lattice.core.application.career.dto.StarRecord;
import com.lattice.core.domain.career.CareerNode;
import com.lattice.core.domain.career.CareerType;
import com.lattice.core.infrastructure.observability.AiUsageMonitor;
import com.lattice.core.infrastructure.prompt.PromptRegistry;
import com.lattice.core.repository.career.CareerRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = CareerService.class)
class CareerServiceTest {

    @MockBean
    CareerRepository repository;
    @MockBean
    ChatModel chatModel;
    @MockBean
    EmbeddingModel embeddingModel;
    @MockBean
    PromptRegistry promptRegistry;
    @MockBean
    AiUsageMonitor aiUsageMonitor;

    @Test
    void shouldPersistStarRecordFromAi() {
        when(promptRegistry.resolveContent("career.star")).thenReturn("prompt");
        ChatResponse response = new ChatResponse(List.of(new Generation(new AssistantMessage("{\"situation\":\"S\",\"task\":\"T\",\"action\":\"A\",\"result\":\"R\"}"))));
        
        when(chatModel.call(any(Prompt.class))).thenReturn(response);
        when(embeddingModel.embed(any(String.class))).thenReturn(new float[]{0.1f, 0.2f});

        CareerService service = new CareerService(repository, chatModel, embeddingModel, promptRegistry, aiUsageMonitor);
        service.createLog("raw", CareerType.WORK_LOG, List.of("tag"));
        Mockito.verify(repository).save(any(CareerNode.class));
    }
}
