package com.lattice.agent.config;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.lattice.core.domain.DomainType;
import com.lattice.retrieval.api.SearchRequest;
import com.lattice.retrieval.service.RetrievalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class KnowledgeToolsConfig {

    public record SearchRequestParams(
            @JsonPropertyDescription("搜索关键词") String query,
            @JsonPropertyDescription("领域类型，如 CAREER, FINANCE, DIY, LIFE") String domain
    ) {}

    @Bean
    @Description("在用户的生活知识库（Lattice）中搜索相关信息")
    public Function<SearchRequestParams, String> searchLattice(RetrievalService retrievalService) {
        return request -> {
            log.info("Tool 'searchLattice' called with: {}", request);
            try {
                DomainType domain = DomainType.valueOf(request.domain().toUpperCase());
                var results = retrievalService.search(new SearchRequest(
                        request.query(),
                        domain,
                        null,
                        5,
                        0.4d
                ));
                
                log.info("Search found {} results.", results.results().size());

                if (results.results().isEmpty()) {
                    return "未在库中找到相关信息。";
                }

                return results.results().stream()
                        .map(hit -> "[标题: " + hit.title() + "] 内容片段: " + hit.snippet())
                        .collect(Collectors.joining("\n---\n"));
            } catch (Exception e) {
                log.error("Error executing 'searchLattice'", e);
                return "搜索执行出错: " + e.getMessage();
            }
        };
    }
}
