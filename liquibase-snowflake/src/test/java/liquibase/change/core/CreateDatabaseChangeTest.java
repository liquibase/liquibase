package liquibase.change.core;

import liquibase.change.Change;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateDatabaseStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for CreateDatabaseChange
 */
@DisplayName("CreateDatabaseChange")
public class CreateDatabaseChangeTest {
    
    private CreateDatabaseChange change;
    private SnowflakeDatabase database;
    
    @BeforeEach
    void setUp() {
        change = new CreateDatabaseChange();
        database = new SnowflakeDatabase();
    }
    
    @Test
    @DisplayName("Should support Snowflake database")
    void shouldSupportSnowflake() {
        assertTrue(change.supports(database));
    }
    
    @Test
    @DisplayName("Should support rollback")
    void shouldSupportRollback() {
        assertTrue(change.supportsRollback(database));
    }
    
    @Test
    @DisplayName("Should create inverse as DropDatabaseChange")
    void shouldCreateDropDatabaseInverse() {
        change.setDatabaseName("TEST_DB");
        
        Change[] inverses = change.createInverses();
        assertEquals(1, inverses.length);
        assertTrue(inverses[0] instanceof DropDatabaseChange);
        assertEquals("TEST_DB", ((DropDatabaseChange) inverses[0]).getDatabaseName());
    }
    
    @Test
    @DisplayName("Should generate basic CREATE DATABASE statement")
    void shouldGenerateBasicStatement() {
        change.setDatabaseName("TEST_DB");
        
        SqlStatement[] statements = change.generateStatements(database);
        assertEquals(1, statements.length);
        
        CreateDatabaseStatement stmt = (CreateDatabaseStatement) statements[0];
        assertEquals("TEST_DB", stmt.getDatabaseName());
        assertNull(stmt.getComment());
        assertNull(stmt.getTransient());
    }
    
    @Test
    @DisplayName("Should generate statement with all attributes")
    void shouldGenerateStatementWithAllAttributes() {
        change.setDatabaseName("FULL_DB");
        change.setComment("Test database");
        change.setDataRetentionTimeInDays("7");
        change.setMaxDataExtensionTimeInDays("30");
        change.setTransient(false);
        change.setDefaultDdlCollation("en-ci");
        change.setOrReplace(true);
        change.setIfNotExists(false);
        change.setCloneFrom("SOURCE_DB");
        
        SqlStatement[] statements = change.generateStatements(database);
        CreateDatabaseStatement stmt = (CreateDatabaseStatement) statements[0];
        
        assertEquals("FULL_DB", stmt.getDatabaseName());
        assertEquals("Test database", stmt.getComment());
        assertEquals("7", stmt.getDataRetentionTimeInDays());
        assertEquals("30", stmt.getMaxDataExtensionTimeInDays());
        assertEquals(false, stmt.getTransient());
        assertEquals("en-ci", stmt.getDefaultDdlCollation());
        assertEquals(true, stmt.getOrReplace());
        assertEquals(false, stmt.getIfNotExists());
        assertEquals("SOURCE_DB", stmt.getCloneFrom());
    }
    
    @Test
    @DisplayName("Should validate database name is required")
    void shouldValidateDatabaseNameRequired() {
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().get(0).contains("databaseName is required"));
    }
    
    @Test
    @DisplayName("Should validate OR REPLACE and IF NOT EXISTS are mutually exclusive")
    void shouldValidateOrReplaceAndIfNotExistsMutuallyExclusive() {
        change.setDatabaseName("TEST_DB");
        change.setOrReplace(true);
        change.setIfNotExists(true);
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Cannot use both OR REPLACE and IF NOT EXISTS")));
    }
    
    @Test
    @DisplayName("Should validate transient databases must have 0 retention")
    void shouldValidateTransientDatabaseRetention() {
        change.setDatabaseName("TRANSIENT_DB");
        change.setTransient(true);
        change.setDataRetentionTimeInDays("7");
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Transient databases must have DATA_RETENTION_TIME_IN_DAYS = 0")));
    }
    
    @Test
    @DisplayName("Should allow transient database with 0 retention")
    void shouldAllowTransientDatabaseWithZeroRetention() {
        change.setDatabaseName("TRANSIENT_DB");
        change.setTransient(true);
        change.setDataRetentionTimeInDays("0");
        
        ValidationErrors errors = change.validate(database);
        
        assertFalse(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should allow transient database with no retention specified")
    void shouldAllowTransientDatabaseWithNoRetention() {
        change.setDatabaseName("TRANSIENT_DB");
        change.setTransient(true);
        // Don't set retention time
        
        ValidationErrors errors = change.validate(database);
        
        assertFalse(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should validate invalid retention time format")
    void shouldValidateInvalidRetentionTimeFormat() {
        change.setDatabaseName("TEST_DB");
        change.setTransient(true);
        change.setDataRetentionTimeInDays("invalid");
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Invalid dataRetentionTimeInDays value")));
    }
    
    @Test
    @DisplayName("Should generate confirmation message")
    void shouldGenerateConfirmationMessage() {
        change.setDatabaseName("TEST_DB");
        
        assertEquals("Database TEST_DB created", change.getConfirmationMessage());
    }
    
    @Test
    @DisplayName("Should use correct XML namespace")
    void shouldUseCorrectNamespace() {
        assertEquals("http://www.liquibase.org/xml/ns/snowflake", change.getSerializedObjectNamespace());
    }
    
    @Test
    @DisplayName("Should handle clone from")
    void shouldHandleCloneFrom() {
        change.setDatabaseName("CLONED_DB");
        change.setCloneFrom("SOURCE_DB");
        
        SqlStatement[] statements = change.generateStatements(database);
        CreateDatabaseStatement stmt = (CreateDatabaseStatement) statements[0];
        
        assertEquals("SOURCE_DB", stmt.getCloneFrom());
    }
    
    @Test
    @DisplayName("Should allow OR REPLACE without IF NOT EXISTS")
    void shouldAllowOrReplaceAlone() {
        change.setDatabaseName("TEST_DB");
        change.setOrReplace(true);
        change.setIfNotExists(false);
        
        ValidationErrors errors = change.validate(database);
        assertFalse(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should allow IF NOT EXISTS without OR REPLACE")
    void shouldAllowIfNotExistsAlone() {
        change.setDatabaseName("TEST_DB");
        change.setOrReplace(false);
        change.setIfNotExists(true);
        
        ValidationErrors errors = change.validate(database);
        assertFalse(errors.hasErrors());
    }
}