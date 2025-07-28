package liquibase.change.core;

import liquibase.change.ChangeFactory;
import liquibase.change.ChangeMetaData;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateSchemaStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CreateSchemaChange
 */
@DisplayName("CreateSchemaChange")
public class CreateSchemaChangeTest {
    
    @Test
    @DisplayName("Should set and get all basic properties correctly")
    public void testBasicProperties() {
        CreateSchemaChange change = new CreateSchemaChange();
        
        // Test required property
        assertNull(change.getSchemaName());
        change.setSchemaName("TEST_SCHEMA");
        assertEquals("TEST_SCHEMA", change.getSchemaName());
        
        // Test optional properties
        assertNull(change.getComment());
        change.setComment("Test comment");
        assertEquals("Test comment", change.getComment());
        
        assertNull(change.getTransient());
        change.setTransient(true);
        assertTrue(change.getTransient());
        
        assertNull(change.getManagedAccess());
        change.setManagedAccess(true);
        assertTrue(change.getManagedAccess());
        
        assertNull(change.getDataRetentionTimeInDays());
        change.setDataRetentionTimeInDays("7");
        assertEquals("7", change.getDataRetentionTimeInDays());
        
        assertNull(change.getOrReplace());
        change.setOrReplace(true);
        assertTrue(change.getOrReplace());
        
        assertNull(change.getIfNotExists());
        change.setIfNotExists(true);
        assertTrue(change.getIfNotExists());
    }
    
    @Test
    @DisplayName("Should only support Snowflake database")
    public void testSupports() {
        CreateSchemaChange change = new CreateSchemaChange();
        
        assertTrue(change.supports(new SnowflakeDatabase()));
        assertFalse(change.supports(new PostgresDatabase()));
    }
    
    @Test
    @DisplayName("Should generate correct statement with all properties")
    public void testGenerateStatements() {
        CreateSchemaChange change = new CreateSchemaChange();
        change.setSchemaName("TEST_SCHEMA");
        change.setComment("Test comment");
        change.setTransient(true);
        change.setManagedAccess(true);
        change.setDataRetentionTimeInDays("0");
        
        SqlStatement[] statements = change.generateStatements(new SnowflakeDatabase());
        
        assertEquals(1, statements.length);
        assertTrue(statements[0] instanceof CreateSchemaStatement);
        
        CreateSchemaStatement stmt = (CreateSchemaStatement) statements[0];
        assertEquals("TEST_SCHEMA", stmt.getSchemaName());
        assertEquals("Test comment", stmt.getComment());
        assertTrue(stmt.getTransient());
        assertTrue(stmt.getManaged());
        assertEquals("0", stmt.getDataRetentionTimeInDays());
    }
    
    @Test
    @DisplayName("Should fail validation when schemaName is missing")
    public void testValidationFailsWithoutSchemaName() {
        CreateSchemaChange change = new CreateSchemaChange();
        
        ValidationErrors errors = change.validate(new SnowflakeDatabase());
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("schemaName is required")));
    }
    
    @Test
    @DisplayName("Should fail validation when orReplace and ifNotExists are both true")
    public void testValidationFailsWithMutuallyExclusiveOptions() {
        CreateSchemaChange change = new CreateSchemaChange();
        change.setSchemaName("TEST_SCHEMA");
        change.setOrReplace(true);
        change.setIfNotExists(true);
        
        ValidationErrors errors = change.validate(new SnowflakeDatabase());
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Cannot use both OR REPLACE and IF NOT EXISTS")));
    }
    
    @Test
    @DisplayName("Should support rollback and create inverse")
    public void testRollbackSupport() {
        CreateSchemaChange change = new CreateSchemaChange();
        change.setSchemaName("TEST_SCHEMA");
        
        assertTrue(change.supportsRollback(new SnowflakeDatabase()));
        
        liquibase.change.Change[] inverses = change.createInverses();
        assertEquals(1, inverses.length);
        assertTrue(inverses[0] instanceof DropSchemaChange);
        
        DropSchemaChange inverse = (DropSchemaChange) inverses[0];
        assertEquals("TEST_SCHEMA", inverse.getSchemaName());
    }
    
    @Test
    @DisplayName("Should return correct confirmation message")
    public void testConfirmationMessage() {
        CreateSchemaChange change = new CreateSchemaChange();
        change.setSchemaName("TEST_SCHEMA");
        
        assertEquals("Schema TEST_SCHEMA created", change.getConfirmationMessage());
    }
    
    @Test
    @DisplayName("Should return correct serialized namespace")
    public void testSerializedNamespace() {
        CreateSchemaChange change = new CreateSchemaChange();
        
        assertEquals("http://www.liquibase.org/xml/ns/snowflake", 
                     change.getSerializedObjectNamespace());
    }
    
    @Test
    @DisplayName("Should be registered in ChangeFactory")
    public void testChangeRegistration() {
        // This test verifies the change is properly registered via ServiceLoader
        ChangeMetaData metadata = ChangeFactory.getInstance()
            .getChangeMetaData("createSchema");
        
        assertNotNull(metadata);
        assertEquals("createSchema", metadata.getName());
        assertEquals("Creates a schema", metadata.getDescription());
    }
}