package liquibase.change.core;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.snowflake.DropSequenceStatementSnowflake;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DropSequenceChange.
 */
@DisplayName("DropSequenceChange Tests")
class DropSequenceChangeTest {

    private DropSequenceChange dropSequenceChange;
    private SnowflakeDatabase database;

    @BeforeEach
    void setUp() {
        dropSequenceChange = new DropSequenceChange();
        database = new SnowflakeDatabase();
    }

    @Test
    @DisplayName("Should generate DropSequenceStatementSnowflake")
    void shouldGenerateDropSequenceStatementSnowflake() {
        // Given
        dropSequenceChange.setSequenceName("TEST_SEQUENCE");

        // When
        SqlStatement[] statements = dropSequenceChange.generateStatements(database);

        // Then
        assertEquals(1, statements.length, "Should generate exactly one statement");
        assertTrue(statements[0] instanceof DropSequenceStatementSnowflake, "Should generate DropSequenceStatementSnowflake");

        DropSequenceStatementSnowflake statement = (DropSequenceStatementSnowflake) statements[0];
        assertEquals("TEST_SEQUENCE", statement.getSequenceName());
    }

    @Test
    @DisplayName("Should generate DropSequenceStatementSnowflake with IF EXISTS")
    void shouldGenerateDropSequenceStatementWithIfExists() {
        // Given
        dropSequenceChange.setSequenceName("TEST_SEQUENCE");
        dropSequenceChange.setIfExists(true);

        // When
        SqlStatement[] statements = dropSequenceChange.generateStatements(database);

        // Then
        assertEquals(1, statements.length, "Should generate exactly one statement");
        assertTrue(statements[0] instanceof DropSequenceStatementSnowflake, "Should generate DropSequenceStatementSnowflake");

        DropSequenceStatementSnowflake statement = (DropSequenceStatementSnowflake) statements[0];
        assertEquals("TEST_SEQUENCE", statement.getSequenceName());
        assertEquals(Boolean.TRUE, statement.getIfExists());
    }

    @Test
    @DisplayName("Should generate DropSequenceStatementSnowflake with CASCADE")
    void shouldGenerateDropSequenceStatementWithCascade() {
        // Given
        dropSequenceChange.setSequenceName("TEST_SEQUENCE");
        dropSequenceChange.setCascade(true);

        // When
        SqlStatement[] statements = dropSequenceChange.generateStatements(database);

        // Then
        assertEquals(1, statements.length, "Should generate exactly one statement");
        assertTrue(statements[0] instanceof DropSequenceStatementSnowflake, "Should generate DropSequenceStatementSnowflake");

        DropSequenceStatementSnowflake statement = (DropSequenceStatementSnowflake) statements[0];
        assertEquals("TEST_SEQUENCE", statement.getSequenceName());
        assertEquals(Boolean.TRUE, statement.getCascade());
    }

    @Test
    @DisplayName("Should generate DropSequenceStatementSnowflake with RESTRICT")
    void shouldGenerateDropSequenceStatementWithRestrict() {
        // Given
        dropSequenceChange.setSequenceName("TEST_SEQUENCE");
        dropSequenceChange.setRestrict(true);

        // When
        SqlStatement[] statements = dropSequenceChange.generateStatements(database);

        // Then
        assertEquals(1, statements.length, "Should generate exactly one statement");
        assertTrue(statements[0] instanceof DropSequenceStatementSnowflake, "Should generate DropSequenceStatementSnowflake");

        DropSequenceStatementSnowflake statement = (DropSequenceStatementSnowflake) statements[0];
        assertEquals("TEST_SEQUENCE", statement.getSequenceName());
        assertEquals(Boolean.TRUE, statement.getRestrict());
    }

    @Test
    @DisplayName("Should handle all drop options")
    void shouldHandleAllDropOptions() {
        // Given
        dropSequenceChange.setCatalogName("MY_CATALOG");
        dropSequenceChange.setSchemaName("MY_SCHEMA");
        dropSequenceChange.setSequenceName("COMPREHENSIVE_SEQUENCE");
        dropSequenceChange.setIfExists(true);
        dropSequenceChange.setCascade(true);

        // When
        SqlStatement[] statements = dropSequenceChange.generateStatements(database);

        // Then
        assertEquals(1, statements.length);
        DropSequenceStatementSnowflake statement = (DropSequenceStatementSnowflake) statements[0];
        
        assertEquals("MY_CATALOG", statement.getCatalogName());
        assertEquals("MY_SCHEMA", statement.getSchemaName());
        assertEquals("COMPREHENSIVE_SEQUENCE", statement.getSequenceName());
        assertEquals(Boolean.TRUE, statement.getIfExists());
        assertEquals(Boolean.TRUE, statement.getCascade());
    }

    @Test
    @DisplayName("Should validate required sequence name")
    void shouldValidateRequiredSequenceName() {
        // Given - no sequence name set
        
        // When
        ValidationErrors errors = dropSequenceChange.validate(database);

        // Then
        assertTrue(errors.hasErrors(), "Should have validation errors");
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("sequenceName is required")),
            "Should contain sequence name required error");
    }

    @Test
    @DisplayName("Should validate mutually exclusive CASCADE and RESTRICT options")
    void shouldValidateMutuallyExclusiveOptions() {
        // Given
        dropSequenceChange.setSequenceName("TEST_SEQUENCE");
        dropSequenceChange.setCascade(true);
        dropSequenceChange.setRestrict(true);

        // When
        ValidationErrors errors = dropSequenceChange.validate(database);

        // Then
        assertTrue(errors.hasErrors(), "Should have validation errors");
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Cannot use both CASCADE and RESTRICT")),
            "Should contain mutually exclusive options error");
    }

    @Test
    @DisplayName("Should generate confirmation message")
    void shouldGenerateConfirmationMessage() {
        // Given
        dropSequenceChange.setSequenceName("MY_SEQUENCE");

        // When
        String message = dropSequenceChange.getConfirmationMessage();

        // Then
        assertEquals("Sequence MY_SEQUENCE dropped", message);
    }

    @Test
    @DisplayName("Should return empty statements for non-Snowflake database")
    void shouldReturnEmptyStatementsForNonSnowflakeDatabase() {
        // Given
        dropSequenceChange.setSequenceName("TEST_SEQUENCE");
        liquibase.database.core.PostgresDatabase postgresDatabase = new liquibase.database.core.PostgresDatabase();

        // When
        SqlStatement[] statements = dropSequenceChange.generateStatements(postgresDatabase);

        // Then
        assertEquals(0, statements.length, "Should return empty statements for non-Snowflake database");
    }
}