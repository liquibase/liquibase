package liquibase.change.core;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.snowflake.AlterSequenceStatementSnowflake;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AlterSequenceChange.
 */
@DisplayName("AlterSequenceChange Tests")
class AlterSequenceChangeTest {

    private AlterSequenceChange alterSequenceChange;
    private SnowflakeDatabase database;

    @BeforeEach
    void setUp() {
        alterSequenceChange = new AlterSequenceChange();
        database = new SnowflakeDatabase();
    }

    @Test
    @DisplayName("Should generate AlterSequenceStatementSnowflake for rename operation")
    void shouldGenerateAlterSequenceStatementForRename() {
        // Given
        alterSequenceChange.setSequenceName("OLD_SEQUENCE");
        alterSequenceChange.setNewSequenceName("NEW_SEQUENCE");

        // When
        SqlStatement[] statements = alterSequenceChange.generateStatements(database);

        // Then
        assertEquals(1, statements.length, "Should generate exactly one statement");
        assertTrue(statements[0] instanceof AlterSequenceStatementSnowflake, "Should generate AlterSequenceStatementSnowflake");

        AlterSequenceStatementSnowflake statement = (AlterSequenceStatementSnowflake) statements[0];
        assertEquals("OLD_SEQUENCE", statement.getSequenceName());
        assertEquals("NEW_SEQUENCE", statement.getNewSequenceName());
    }

    @Test
    @DisplayName("Should generate AlterSequenceStatementSnowflake for increment change")
    void shouldGenerateAlterSequenceStatementForIncrement() {
        // Given
        alterSequenceChange.setSequenceName("TEST_SEQUENCE");
        alterSequenceChange.setIncrementBy(BigInteger.valueOf(10));

        // When
        SqlStatement[] statements = alterSequenceChange.generateStatements(database);

        // Then
        assertEquals(1, statements.length, "Should generate exactly one statement");
        assertTrue(statements[0] instanceof AlterSequenceStatementSnowflake, "Should generate AlterSequenceStatementSnowflake");

        AlterSequenceStatementSnowflake statement = (AlterSequenceStatementSnowflake) statements[0];
        assertEquals("TEST_SEQUENCE", statement.getSequenceName());
        assertEquals(BigInteger.valueOf(10), statement.getIncrementBy());
    }

    @Test
    @DisplayName("Should generate AlterSequenceStatementSnowflake for ordering change")
    void shouldGenerateAlterSequenceStatementForOrdering() {
        // Given
        alterSequenceChange.setSequenceName("TEST_SEQUENCE");
        alterSequenceChange.setOrdered(true);

        // When
        SqlStatement[] statements = alterSequenceChange.generateStatements(database);

        // Then
        assertEquals(1, statements.length, "Should generate exactly one statement");
        assertTrue(statements[0] instanceof AlterSequenceStatementSnowflake, "Should generate AlterSequenceStatementSnowflake");

        AlterSequenceStatementSnowflake statement = (AlterSequenceStatementSnowflake) statements[0];
        assertEquals("TEST_SEQUENCE", statement.getSequenceName());
        assertEquals(Boolean.TRUE, statement.getOrdered());
    }

    @Test
    @DisplayName("Should generate AlterSequenceStatementSnowflake for comment change")
    void shouldGenerateAlterSequenceStatementForComment() {
        // Given
        alterSequenceChange.setSequenceName("TEST_SEQUENCE");
        alterSequenceChange.setComment("Updated comment");

        // When
        SqlStatement[] statements = alterSequenceChange.generateStatements(database);

        // Then
        assertEquals(1, statements.length, "Should generate exactly one statement");
        assertTrue(statements[0] instanceof AlterSequenceStatementSnowflake, "Should generate AlterSequenceStatementSnowflake");

        AlterSequenceStatementSnowflake statement = (AlterSequenceStatementSnowflake) statements[0];
        assertEquals("TEST_SEQUENCE", statement.getSequenceName());
        assertEquals("Updated comment", statement.getComment());
    }

    @Test
    @DisplayName("Should generate AlterSequenceStatementSnowflake for unset comment")
    void shouldGenerateAlterSequenceStatementForUnsetComment() {
        // Given
        alterSequenceChange.setSequenceName("TEST_SEQUENCE");
        alterSequenceChange.setUnsetComment(true);

        // When
        SqlStatement[] statements = alterSequenceChange.generateStatements(database);

        // Then
        assertEquals(1, statements.length, "Should generate exactly one statement");
        assertTrue(statements[0] instanceof AlterSequenceStatementSnowflake, "Should generate AlterSequenceStatementSnowflake");

        AlterSequenceStatementSnowflake statement = (AlterSequenceStatementSnowflake) statements[0];
        assertEquals("TEST_SEQUENCE", statement.getSequenceName());
        assertEquals(Boolean.TRUE, statement.getUnsetComment());
    }

    @Test
    @DisplayName("Should handle multiple alterations in one change")
    void shouldHandleMultipleAlterations() {
        // Given
        alterSequenceChange.setCatalogName("MY_CATALOG");
        alterSequenceChange.setSchemaName("MY_SCHEMA");
        alterSequenceChange.setSequenceName("COMPREHENSIVE_SEQUENCE");
        alterSequenceChange.setIncrementBy(BigInteger.valueOf(25));
        alterSequenceChange.setOrdered(false); // NOORDER
        alterSequenceChange.setComment("Comprehensive alteration test");
        alterSequenceChange.setIfExists(true);

        // When
        SqlStatement[] statements = alterSequenceChange.generateStatements(database);

        // Then
        assertEquals(1, statements.length);
        AlterSequenceStatementSnowflake statement = (AlterSequenceStatementSnowflake) statements[0];
        
        assertEquals("MY_CATALOG", statement.getCatalogName());
        assertEquals("MY_SCHEMA", statement.getSchemaName());
        assertEquals("COMPREHENSIVE_SEQUENCE", statement.getSequenceName());
        assertEquals(BigInteger.valueOf(25), statement.getIncrementBy());
        assertEquals(Boolean.FALSE, statement.getOrdered());
        assertEquals("Comprehensive alteration test", statement.getComment());
        assertEquals(Boolean.TRUE, statement.getIfExists());
    }

    @Test
    @DisplayName("Should validate required sequence name")
    void shouldValidateRequiredSequenceName() {
        // Given - no sequence name set
        alterSequenceChange.setIncrementBy(BigInteger.valueOf(5));
        
        // When
        ValidationErrors errors = alterSequenceChange.validate(database);

        // Then
        assertTrue(errors.hasErrors(), "Should have validation errors");
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("sequenceName is required")),
            "Should contain sequence name required error");
    }

    @Test
    @DisplayName("Should validate at least one alteration is specified")
    void shouldValidateAtLeastOneAlteration() {
        // Given
        alterSequenceChange.setSequenceName("TEST_SEQUENCE");
        // No alterations set
        
        // When
        ValidationErrors errors = alterSequenceChange.validate(database);

        // Then
        assertTrue(errors.hasErrors(), "Should have validation errors");
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("At least one alteration must be specified")),
            "Should contain at least one alteration required error");
    }

    @Test
    @DisplayName("Should validate conflicting comment operations")
    void shouldValidateConflictingCommentOperations() {
        // Given
        alterSequenceChange.setSequenceName("TEST_SEQUENCE");
        alterSequenceChange.setComment("New comment");
        alterSequenceChange.setUnsetComment(true);

        // When
        ValidationErrors errors = alterSequenceChange.validate(database);

        // Then
        assertTrue(errors.hasErrors(), "Should have validation errors");
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Cannot both set comment and unset comment")),
            "Should contain comment conflict error");
    }

    @Test
    @DisplayName("Should generate rename confirmation message")
    void shouldGenerateRenameConfirmationMessage() {
        // Given
        alterSequenceChange.setSequenceName("OLD_SEQUENCE");
        alterSequenceChange.setNewSequenceName("NEW_SEQUENCE");

        // When
        String message = alterSequenceChange.getConfirmationMessage();

        // Then
        assertEquals("Sequence OLD_SEQUENCE renamed to NEW_SEQUENCE", message);
    }

    @Test
    @DisplayName("Should generate alter confirmation message")
    void shouldGenerateAlterConfirmationMessage() {
        // Given
        alterSequenceChange.setSequenceName("MY_SEQUENCE");
        alterSequenceChange.setIncrementBy(BigInteger.valueOf(5));

        // When
        String message = alterSequenceChange.getConfirmationMessage();

        // Then
        assertEquals("Sequence MY_SEQUENCE altered", message);
    }

    @Test
    @DisplayName("Should return empty statements for non-Snowflake database")
    void shouldReturnEmptyStatementsForNonSnowflakeDatabase() {
        // Given
        alterSequenceChange.setSequenceName("TEST_SEQUENCE");
        alterSequenceChange.setIncrementBy(BigInteger.valueOf(5));
        liquibase.database.core.PostgresDatabase postgresDatabase = new liquibase.database.core.PostgresDatabase();

        // When
        SqlStatement[] statements = alterSequenceChange.generateStatements(postgresDatabase);

        // Then
        assertEquals(0, statements.length, "Should return empty statements for non-Snowflake database");
    }
}