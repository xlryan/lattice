package com.lattice.core.repository.career;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lattice.core.domain.career.CareerNode;
import com.lattice.core.domain.career.CareerType;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan("com.lattice.core.domain")
@EnableJpaRepositories("com.lattice.core.repository")
@Import(ObjectMapper.class)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.type.wrapper_array_handling=LEGACY",
        "spring.flyway.locations=classpath:db/migration",
        "spring.flyway.clean-disabled=false"
})
class CareerRepositoryTest {

    @org.springframework.boot.SpringBootConfiguration
    @org.springframework.boot.autoconfigure.EnableAutoConfiguration
    static class TestConfig {
    }

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("pgvector/pgvector:pg16")
            .withDatabaseName("lattice")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void overrideDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    CareerRepository repository;

    @Autowired
    Flyway flyway;

    @Autowired
    DataSource dataSource;

    @BeforeEach
    void migrateSchema() {
        resetSchema();
        flyway.migrate();
    }

    private void resetSchema() {
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement()) {
            stmt.execute("DROP SCHEMA IF EXISTS lattice CASCADE");
            stmt.execute("DROP EXTENSION IF EXISTS vector CASCADE");
            stmt.execute("CREATE SCHEMA lattice");
            stmt.execute("CREATE EXTENSION IF NOT EXISTS vector WITH SCHEMA public");
            stmt.execute("SET search_path TO lattice, public");
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to reset schema", ex);
        }
    }

    @Test
    void shouldPersistJsonbStructuredData() {
        CareerNode node = CareerNode.builder()
                .type(CareerType.WORK_LOG)
                .rawContent("Optimized login endpoint")
                .structuredData(Map.of("situation", "legacy", "task", "optimize"))
                .embedding(mockEmbedding())
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
                .embedding(mockEmbedding())
                .tags(List.of())
                .build();
        CareerNode node2 = CareerNode.builder()
                .type(CareerType.WORK_LOG)
                .rawContent("Vector ops")
                .structuredData(Map.of())
                .embedding(mockEmbedding())
                .tags(List.of())
                .build();
        repository.saveAll(List.of(node1, node2));
        List<CareerNode> result = repository.semanticSearch(List.of(1.0, 0.0, 0.0), 1);
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getRawContent()).isEqualTo("Auth work");
    }

    private List<Double> mockEmbedding() {
        List<Double> vector = new ArrayList<>(1536);
        for (int i = 0; i < 1536; i++) {
            vector.add((double) (i % 5) / 10);
        }
        return vector;
    }
}
