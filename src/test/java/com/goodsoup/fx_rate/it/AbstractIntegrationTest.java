package com.goodsoup.fx_rate.it;

import java.nio.file.Files;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("fx_rate")
            .withUsername("postgres")
            .withPassword("postgres");

    @TempDir
    static java.nio.file.Path tempDir;

    @BeforeAll
    static void ensureTempDirExists() throws Exception {
        Files.createDirectories(tempDir);
    }

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);

        // Keep uploads isolated for tests
        registry.add("app.uploads.dir", () -> tempDir.resolve("uploads").toString());

        // Rabbit isn't needed for these tests; don’t try to connect
        registry.add("spring.rabbitmq.listener.simple.auto-startup", () -> "false");
    }
}

