package org.liquibase.spring.postgresql;

import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext
@Testcontainers
class LiquibaseUpdateTest {

    static final class DatasourceInitializer {

        private static final Logger LOGGER = LoggerFactory.getLogger(DatasourceInitializer.class);

        public static final Logger CONTAINER_LOGGER = LoggerFactory.getLogger("Containerlogger");

        public static void initialize(DynamicPropertyRegistry registry, JdbcDatabaseContainer container) {
            registry.add("spring.datasource.url", container::getJdbcUrl);
            registry.add("spring.datasource.username", container::getUsername);
            registry.add("spring.datasource.password", container::getPassword);
            logDatasourceInfo(container);
        }

        private static void logDatasourceInfo(JdbcDatabaseContainer container) {
            LOGGER.info("################################################");
            LOGGER.info("Using datasource config for tests: ");
            LOGGER.info("URL     : {}", container.getJdbcUrl());
            LOGGER.info("User    : {}", container.getUsername());
            LOGGER.info("Password: {}", container.getPassword());
            LOGGER.info("################################################");
        }
    }

    @Autowired
    private SpringLiquibase liquibase;

    @Container
    protected static final PostgreSQLContainer MY_TEST_DB = new PostgreSQLContainer<>("postgres:16")
            .withLogConsumer(new Slf4jLogConsumer(DatasourceInitializer.CONTAINER_LOGGER))
            .withDatabaseName("TEST");

    @DynamicPropertySource
    static void jdbcProperties(DynamicPropertyRegistry registry) {
        DatasourceInitializer.initialize(registry, MY_TEST_DB);
    }

    @Test
    //@Transactional(Transactional.TxType.NEVER)
    void migrationAddsVweNummer() throws LiquibaseException {
        liquibase.afterPropertiesSet();
    }
}