package liquibase.change.core;

import liquibase.change.Change;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AlterDatabaseStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AlterDatabaseChange
 */
@DisplayName("AlterDatabaseChange")
public class AlterDatabaseChangeTest {
    
    private AlterDatabaseChange change;
    private SnowflakeDatabase database;
    
    @BeforeEach
    void setUp() {
        change = new AlterDatabaseChange();
        database = new SnowflakeDatabase();
    }
    
    @Test
    @DisplayName("Should support Snowflake database")
    void shouldSupportSnowflake() {
        assertTrue(change.supports(database));
    }
    
    @Test
    @DisplayName("Should not support rollback")
    void shouldNotSupportRollback() {
        assertFalse(change.supportsRollback(database));
    }
    
    @Test
    @DisplayName("Should generate rename statement")
    void shouldGenerateRenameStatement() {
        change.setDatabaseName("OLD_DB");
        change.setNewDatabaseName("NEW_DB");
        
        SqlStatement[] statements = change.generateStatements(database);
        assertEquals(1, statements.length);
        
        AlterDatabaseStatement stmt = (AlterDatabaseStatement) statements[0];
        assertEquals("OLD_DB", stmt.getDatabaseName());
        assertEquals("NEW_DB", stmt.getNewName());
    }
    
    @Test
    @DisplayName("Should generate statement with IF EXISTS")
    void shouldGenerateStatementWithIfExists() {
        change.setDatabaseName("TEST_DB");
        change.setIfExists(true);
        change.setDataRetentionTimeInDays("7");
        
        SqlStatement[] statements = change.generateStatements(database);
        AlterDatabaseStatement stmt = (AlterDatabaseStatement) statements[0];
        
        assertEquals(true, stmt.getIfExists());
    }
    
    @Test
    @DisplayName("Should generate SET properties statement")
    void shouldGenerateSetPropertiesStatement() {
        change.setDatabaseName("TEST_DB");
        change.setDataRetentionTimeInDays("7");
        change.setNewMaxDataExtensionTimeInDays("30");
        change.setNewDefaultDdlCollation("en-ci");
        change.setComment("Updated database");
        
        SqlStatement[] statements = change.generateStatements(database);
        AlterDatabaseStatement stmt = (AlterDatabaseStatement) statements[0];
        
        assertEquals("7", stmt.getNewDataRetentionTimeInDays());
        assertEquals("30", stmt.getNewMaxDataExtensionTimeInDays());
        assertEquals("en-ci", stmt.getNewDefaultDdlCollation());
        assertEquals("Updated database", stmt.getNewComment());
    }
    
    @Test
    @DisplayName("Should generate UNSET properties statement")
    void shouldGenerateUnsetPropertiesStatement() {
        change.setDatabaseName("TEST_DB");
        change.setUnsetDataRetentionTimeInDays(true);
        change.setUnsetMaxDataExtensionTimeInDays(true);
        change.setUnsetDefaultDdlCollation(true);
        change.setUnsetComment(true);
        
        SqlStatement[] statements = change.generateStatements(database);
        AlterDatabaseStatement stmt = (AlterDatabaseStatement) statements[0];
        
        assertEquals(true, stmt.getUnsetDataRetentionTimeInDays());
        assertEquals(true, stmt.getUnsetMaxDataExtensionTimeInDays());
        assertEquals(true, stmt.getUnsetDefaultDdlCollation());
        assertEquals(true, stmt.getUnsetComment());
    }
    
    @Test
    @DisplayName("Should validate database name is required")
    void shouldValidateDatabaseNameRequired() {
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().get(0).contains("databaseName is required"));
    }
    
    @Test
    @DisplayName("Should validate at least one change is required")
    void shouldValidateAtLeastOneChangeRequired() {
        change.setDatabaseName("TEST_DB");
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("At least one database property must be changed")));
    }
    
    @Test
    @DisplayName("Should include UNSET operations in validation check")
    void shouldIncludeUnsetInValidationCheck() {
        change.setDatabaseName("TEST_DB");
        change.setUnsetComment(true);
        
        ValidationErrors errors = change.validate(database);
        
        assertFalse(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should validate SET and UNSET mutual exclusivity")
    void shouldValidateSetUnsetMutualExclusivity() {
        change.setDatabaseName("TEST_DB");
        change.setDataRetentionTimeInDays("7");
        change.setUnsetDataRetentionTimeInDays(true);
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Cannot both SET and UNSET dataRetentionTimeInDays")));
    }
    
    @Test
    @DisplayName("Should validate comment and dropComment mutual exclusivity")
    void shouldValidateCommentMutualExclusivity() {
        change.setDatabaseName("TEST_DB");
        change.setComment("New comment");
        change.setDropComment(true);
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Cannot specify both comment and dropComment")));
    }
    
    @Test
    @DisplayName("Should allow rename alone")
    void shouldAllowRenameAlone() {
        change.setDatabaseName("OLD_DB");
        change.setNewDatabaseName("NEW_DB");
        
        ValidationErrors errors = change.validate(database);
        assertFalse(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should allow SET operations alone")
    void shouldAllowSetOperationsAlone() {
        change.setDatabaseName("TEST_DB");
        change.setDataRetentionTimeInDays("7");
        
        ValidationErrors errors = change.validate(database);
        assertFalse(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should allow UNSET operations alone")
    void shouldAllowUnsetOperationsAlone() {
        change.setDatabaseName("TEST_DB");
        change.setUnsetComment(true);
        
        ValidationErrors errors = change.validate(database);
        assertFalse(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should generate confirmation message")
    void shouldGenerateConfirmationMessage() {
        change.setDatabaseName("TEST_DB");
        
        assertEquals("Database TEST_DB altered", change.getConfirmationMessage());
    }
    
    @Test
    @DisplayName("Should use correct XML namespace")
    void shouldUseCorrectNamespace() {
        assertEquals("http://www.liquibase.org/xml/ns/snowflake", change.getSerializedObjectNamespace());
    }
}