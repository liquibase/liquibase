package liquibase.change.core;

import liquibase.change.Change;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropDatabaseStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DropDatabaseChange
 */
@DisplayName("DropDatabaseChange")
public class DropDatabaseChangeTest {
    
    private DropDatabaseChange change;
    private SnowflakeDatabase database;
    
    @BeforeEach
    void setUp() {
        change = new DropDatabaseChange();
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
    @DisplayName("Should generate basic DROP DATABASE statement")
    void shouldGenerateBasicStatement() {
        change.setDatabaseName("TEST_DB");
        
        SqlStatement[] statements = change.generateStatements(database);
        assertEquals(1, statements.length);
        
        DropDatabaseStatement stmt = (DropDatabaseStatement) statements[0];
        assertEquals("TEST_DB", stmt.getDatabaseName());
        assertNull(stmt.getIfExists());
        assertNull(stmt.getCascade());
        assertNull(stmt.getRestrict());
    }
    
    @Test
    @DisplayName("Should generate statement with IF EXISTS")
    void shouldGenerateStatementWithIfExists() {
        change.setDatabaseName("TEST_DB");
        change.setIfExists(true);
        
        SqlStatement[] statements = change.generateStatements(database);
        DropDatabaseStatement stmt = (DropDatabaseStatement) statements[0];
        
        assertEquals("TEST_DB", stmt.getDatabaseName());
        assertEquals(true, stmt.getIfExists());
    }
    
    @Test
    @DisplayName("Should generate statement with CASCADE")
    void shouldGenerateStatementWithCascade() {
        change.setDatabaseName("TEST_DB");
        change.setCascade(true);
        
        SqlStatement[] statements = change.generateStatements(database);
        DropDatabaseStatement stmt = (DropDatabaseStatement) statements[0];
        
        assertEquals("TEST_DB", stmt.getDatabaseName());
        assertEquals(true, stmt.getCascade());
    }
    
    @Test
    @DisplayName("Should generate statement with RESTRICT")
    void shouldGenerateStatementWithRestrict() {
        change.setDatabaseName("TEST_DB");
        change.setRestrict(true);
        
        SqlStatement[] statements = change.generateStatements(database);
        DropDatabaseStatement stmt = (DropDatabaseStatement) statements[0];
        
        assertEquals("TEST_DB", stmt.getDatabaseName());
        assertEquals(true, stmt.getRestrict());
    }
    
    @Test
    @DisplayName("Should validate database name is required")
    void shouldValidateDatabaseNameRequired() {
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().get(0).contains("databaseName is required"));
    }
    
    @Test
    @DisplayName("Should validate CASCADE and RESTRICT are mutually exclusive")
    void shouldValidateCascadeRestrictMutuallyExclusive() {
        change.setDatabaseName("TEST_DB");
        change.setCascade(true);
        change.setRestrict(true);
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Cannot specify both CASCADE and RESTRICT")));
    }
    
    @Test
    @DisplayName("Should allow CASCADE without RESTRICT")
    void shouldAllowCascadeAlone() {
        change.setDatabaseName("TEST_DB");
        change.setCascade(true);
        change.setRestrict(false);
        
        ValidationErrors errors = change.validate(database);
        assertFalse(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should allow RESTRICT without CASCADE")
    void shouldAllowRestrictAlone() {
        change.setDatabaseName("TEST_DB");
        change.setCascade(false);
        change.setRestrict(true);
        
        ValidationErrors errors = change.validate(database);
        assertFalse(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should allow neither CASCADE nor RESTRICT")
    void shouldAllowNeitherCascadeNorRestrict() {
        change.setDatabaseName("TEST_DB");
        // Don't set cascade or restrict - defaults
        
        ValidationErrors errors = change.validate(database);
        assertFalse(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should generate confirmation message")
    void shouldGenerateConfirmationMessage() {
        change.setDatabaseName("TEST_DB");
        
        assertEquals("Database TEST_DB dropped", change.getConfirmationMessage());
    }
    
    @Test
    @DisplayName("Should use correct XML namespace")
    void shouldUseCorrectNamespace() {
        assertEquals("http://www.liquibase.org/xml/ns/snowflake", change.getSerializedObjectNamespace());
    }
    
    @Test
    @DisplayName("Should handle all attributes")
    void shouldHandleAllAttributes() {
        change.setDatabaseName("FULL_DB");
        change.setIfExists(true);
        change.setCascade(true);
        
        SqlStatement[] statements = change.generateStatements(database);
        DropDatabaseStatement stmt = (DropDatabaseStatement) statements[0];
        
        assertEquals("FULL_DB", stmt.getDatabaseName());
        assertEquals(true, stmt.getIfExists());
        assertEquals(true, stmt.getCascade());
        assertNull(stmt.getRestrict());
    }
}