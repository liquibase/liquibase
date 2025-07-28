package liquibase.change.core;

import liquibase.change.ChangeFactory;
import liquibase.change.ChangeMetaData;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropSchemaStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DropSchemaChange
 */
@DisplayName("DropSchemaChange")
public class DropSchemaChangeTest {
    
    @Test
    @DisplayName("Should set and get all basic properties correctly")
    public void testBasicProperties() {
        DropSchemaChange change = new DropSchemaChange();
        
        // Test required property
        assertNull(change.getSchemaName());
        change.setSchemaName("TEST_SCHEMA");
        assertEquals("TEST_SCHEMA", change.getSchemaName());
        
        // Test optional properties
        assertNull(change.getCascade());
        change.setCascade(true);
        assertTrue(change.getCascade());
        
        assertNull(change.getIfExists());
        change.setIfExists(true);
        assertTrue(change.getIfExists());
        
        assertNull(change.getRestrict());
        change.setRestrict(true);
        assertTrue(change.getRestrict());
    }
    
    @Test
    @DisplayName("Should only support Snowflake database")
    public void testSupports() {
        DropSchemaChange change = new DropSchemaChange();
        
        assertTrue(change.supports(new SnowflakeDatabase()));
        assertFalse(change.supports(new PostgresDatabase()));
    }
    
    @Test
    @DisplayName("Should generate correct statement with all properties")
    public void testGenerateStatements() {
        DropSchemaChange change = new DropSchemaChange();
        change.setSchemaName("TEST_SCHEMA");
        change.setCascade(true);
        change.setIfExists(true);
        
        SqlStatement[] statements = change.generateStatements(new SnowflakeDatabase());
        
        assertEquals(1, statements.length);
        assertTrue(statements[0] instanceof DropSchemaStatement);
        
        DropSchemaStatement stmt = (DropSchemaStatement) statements[0];
        assertEquals("TEST_SCHEMA", stmt.getSchemaName());
        assertTrue(stmt.getCascade());
        assertTrue(stmt.getIfExists());
    }
    
    @Test
    @DisplayName("Should generate statement with restrict option")
    public void testGenerateStatementsWithRestrict() {
        DropSchemaChange change = new DropSchemaChange();
        change.setSchemaName("TEST_SCHEMA");
        change.setRestrict(true);
        
        SqlStatement[] statements = change.generateStatements(new SnowflakeDatabase());
        
        assertEquals(1, statements.length);
        DropSchemaStatement stmt = (DropSchemaStatement) statements[0];
        assertTrue(stmt.getRestrict());
        assertNull(stmt.getCascade());
    }
    
    @Test
    @DisplayName("Should fail validation when schemaName is missing")
    public void testValidationFailsWithoutSchemaName() {
        DropSchemaChange change = new DropSchemaChange();
        
        ValidationErrors errors = change.validate(new SnowflakeDatabase());
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("schemaName is required")));
    }
    
    @Test
    @DisplayName("Should fail validation when cascade and restrict are both set")
    public void testValidationFailsWithMutuallyExclusiveOptions() {
        DropSchemaChange change = new DropSchemaChange();
        change.setSchemaName("TEST_SCHEMA");
        change.setCascade(true);
        change.setRestrict(true);
        
        ValidationErrors errors = change.validate(new SnowflakeDatabase());
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Cannot use both CASCADE and RESTRICT")));
    }
    
    @Test
    @DisplayName("Should not support rollback")
    public void testRollbackSupport() {
        DropSchemaChange change = new DropSchemaChange();
        change.setSchemaName("TEST_SCHEMA");
        
        assertFalse(change.supportsRollback(new SnowflakeDatabase()));
    }
    
    @Test
    @DisplayName("Should return correct confirmation message")
    public void testConfirmationMessage() {
        DropSchemaChange change = new DropSchemaChange();
        change.setSchemaName("TEST_SCHEMA");
        
        assertEquals("Schema TEST_SCHEMA dropped", change.getConfirmationMessage());
    }
    
    @Test
    @DisplayName("Should return correct serialized namespace")
    public void testSerializedNamespace() {
        DropSchemaChange change = new DropSchemaChange();
        
        assertEquals("http://www.liquibase.org/xml/ns/snowflake", 
                     change.getSerializedObjectNamespace());
    }
    
    @Test
    @DisplayName("Should be registered in ChangeFactory")
    public void testChangeRegistration() {
        // This test verifies the change is properly registered via ServiceLoader
        ChangeMetaData metadata = ChangeFactory.getInstance()
            .getChangeMetaData("dropSchema");
        
        assertNotNull(metadata);
        assertEquals("dropSchema", metadata.getName());
        assertEquals("Drops a schema", metadata.getDescription());
    }
}