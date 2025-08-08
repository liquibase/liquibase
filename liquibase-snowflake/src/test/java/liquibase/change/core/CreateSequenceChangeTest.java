package liquibase.change.core;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.snowflake.CreateSequenceStatementSnowflake;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CreateSequenceChange.
 */
@DisplayName("CreateSequenceChange Tests")
class CreateSequenceChangeTest {

    private CreateSequenceChange createSequenceChange;
    private SnowflakeDatabase database;

    @BeforeEach
    void setUp() {
        createSequenceChange = new CreateSequenceChange();
        database = new SnowflakeDatabase();
    }

    @Test
    @DisplayName("Should generate CreateSequenceStatementSnowflake")
    void shouldGenerateCreateSequenceStatementSnowflake() {
        // Given
        createSequenceChange.setSequenceName("TEST_SEQUENCE");
        createSequenceChange.setStartValue(BigInteger.valueOf(100));
        createSequenceChange.setIncrementBy(BigInteger.valueOf(5));
        createSequenceChange.setComment("Test sequence");
        createSequenceChange.setOrdered(true);

        // When
        SqlStatement[] statements = createSequenceChange.generateStatements(database);

        // Then
        assertEquals(1, statements.length, "Should generate exactly one statement");
        assertTrue(statements[0] instanceof CreateSequenceStatementSnowflake, "Should generate CreateSequenceStatementSnowflake");

        CreateSequenceStatementSnowflake statement = (CreateSequenceStatementSnowflake) statements[0];
        assertEquals("TEST_SEQUENCE", statement.getSequenceName());
        assertEquals(BigInteger.valueOf(100), statement.getStartValue());
        assertEquals(BigInteger.valueOf(5), statement.getIncrementBy());
        assertEquals("Test sequence", statement.getComment());
        assertEquals(Boolean.TRUE, statement.getOrder());
    }

    @Test
    @DisplayName("Should validate required sequence name")
    void shouldValidateRequiredSequenceName() {
        // Given - no sequence name set
        
        // When
        ValidationErrors errors = createSequenceChange.validate(database);

        // Then
        assertTrue(errors.hasErrors(), "Should have validation errors");
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("sequenceName is required")),
            "Should contain sequence name required error");
    }

    @Test
    @DisplayName("Should validate mutually exclusive orReplace and ifNotExists")
    void shouldValidateMutuallyExclusiveOptions() {
        // Given
        createSequenceChange.setSequenceName("TEST_SEQUENCE");
        createSequenceChange.setOrReplace(true);
        createSequenceChange.setIfNotExists(true);

        // When
        ValidationErrors errors = createSequenceChange.validate(database);

        // Then
        assertTrue(errors.hasErrors(), "Should have validation errors");
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Cannot use both orReplace and ifNotExists")),
            "Should contain mutually exclusive options error");
    }

    @Test
    @DisplayName("Should generate confirmation message")
    void shouldGenerateConfirmationMessage() {
        // Given
        createSequenceChange.setSequenceName("MY_SEQUENCE");

        // When
        String message = createSequenceChange.getConfirmationMessage();

        // Then
        assertEquals("Sequence MY_SEQUENCE created", message);
    }

    @Test
    @DisplayName("Should handle all sequence properties")
    void shouldHandleAllSequenceProperties() {
        // Given
        createSequenceChange.setCatalogName("MY_CATALOG");
        createSequenceChange.setSchemaName("MY_SCHEMA");
        createSequenceChange.setSequenceName("COMPREHENSIVE_SEQUENCE");
        createSequenceChange.setStartValue(BigInteger.valueOf(500));
        createSequenceChange.setIncrementBy(BigInteger.valueOf(25));
        createSequenceChange.setMinValue(BigInteger.valueOf(1));
        createSequenceChange.setMaxValue(BigInteger.valueOf(999999));
        createSequenceChange.setOrdered(true);
        createSequenceChange.setCycle(false);
        createSequenceChange.setOrReplace(true);
        createSequenceChange.setComment("Comprehensive test sequence");

        // When
        SqlStatement[] statements = createSequenceChange.generateStatements(database);

        // Then
        assertEquals(1, statements.length);
        CreateSequenceStatementSnowflake statement = (CreateSequenceStatementSnowflake) statements[0];
        
        assertEquals("MY_CATALOG", statement.getCatalogName());
        assertEquals("MY_SCHEMA", statement.getSchemaName());
        assertEquals("COMPREHENSIVE_SEQUENCE", statement.getSequenceName());
        assertEquals(BigInteger.valueOf(500), statement.getStartValue());
        assertEquals(BigInteger.valueOf(25), statement.getIncrementBy());
        assertEquals(BigInteger.valueOf(1), statement.getMinValue());
        assertEquals(BigInteger.valueOf(999999), statement.getMaxValue());
        assertEquals(Boolean.TRUE, statement.getOrder());
        assertEquals(Boolean.FALSE, statement.getCycle());
        assertEquals(Boolean.TRUE, statement.getOrReplace());
        assertEquals("Comprehensive test sequence", statement.getComment());
    }

    @Test
    @DisplayName("Should return empty statements for non-Snowflake database")
    void shouldReturnEmptyStatementsForNonSnowflakeDatabase() {
        // Given
        createSequenceChange.setSequenceName("TEST_SEQUENCE");
        liquibase.database.core.PostgresDatabase postgresDatabase = new liquibase.database.core.PostgresDatabase();

        // When
        SqlStatement[] statements = createSequenceChange.generateStatements(postgresDatabase);

        // Then
        assertEquals(0, statements.length, "Should return empty statements for non-Snowflake database");
    }
}