package com.lattice.agent.config;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.lattice.ingest.service.IngestionService;
import com.lattice.ingest.service.dto.IngestionResponse;
import com.lattice.ingest.service.dto.TextIngestionRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.Collections;
import java.util.function.Function;

@Configuration
@Slf4j
public class IngestionToolsConfig {

    public record NoteRequest(
            @JsonPropertyDescription("笔记内容") String content,
            @JsonPropertyDescription("可选：笔记标题") String title
    ) {}

    @Bean
    @Description("保存用户的笔记、日记、想法或任意文本记录到知识库")
    public Function<NoteRequest, String> saveNote(IngestionService ingestionService) {
        return request -> {
            log.info("Tool 'saveNote' called with: {}", request);
            try {
                TextIngestionRequest ingestionRequest = new TextIngestionRequest(
                        request.content(),
                        request.title(),
                        Collections.emptyList()
                );
                
                IngestionResponse response = ingestionService.ingest(ingestionRequest);
                
                log.info("Note ingested successfully. ID: {}, Domain: {}", response.nodeId(), response.domain());
                return "已保存到知识库，归类为: " + response.domain() + "，ID: " + response.nodeId();
            } catch (Exception e) {
                log.error("Error executing 'saveNote'", e);
                return "保存笔记失败: " + e.getMessage();
            }
        };
    }
}
