package com.lattice.core.repository.career;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lattice.core.domain.career.CareerNode;
import com.lattice.core.domain.career.CareerType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ObjectMapper.class)
class CareerRepositoryTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("pgvector/pgvector:pg16")
            .withDatabaseName("lattice")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void overrideDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    CareerRepository repository;

    @Test
    void shouldPersistJsonbStructuredData() {
        CareerNode node = CareerNode.builder()
                .type(CareerType.WORK_LOG)
                .rawContent("Optimized login endpoint")
                .structuredData(Map.of("situation", "legacy", "task", "optimize"))
                .embedding(List.of(0.1, 0.2, 0.3))
                .tags(List.of("login"))
                .build();
        CareerNode saved = repository.save(node);
        assertThat(saved.getId()).isNotNull();
        CareerNode loaded = repository.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getStructuredData().get("task")).isEqualTo("optimize");
    }

    @Test
    void semanticSearchShouldOrderBySimilarity() {
        CareerNode node1 = CareerNode.builder()
                .type(CareerType.WORK_LOG)
                .rawContent("Auth work")
                .structuredData(Map.of())
                .embedding(List.of(1.0, 0.0, 0.0))
                .tags(List.of())
                .build();
        CareerNode node2 = CareerNode.builder()
                .type(CareerType.WORK_LOG)
                .rawContent("Vector ops")
                .structuredData(Map.of())
                .embedding(List.of(0.0, 1.0, 0.0))
                .tags(List.of())
                .build();
        repository.saveAll(List.of(node1, node2));
        List<CareerNode> result = repository.semanticSearch(List.of(1.0, 0.0, 0.0), 1);
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getRawContent()).isEqualTo("Auth work");
    }
}
