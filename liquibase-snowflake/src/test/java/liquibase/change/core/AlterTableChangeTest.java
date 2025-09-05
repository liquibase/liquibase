package liquibase.change.core;

import liquibase.change.ChangeFactory;
import liquibase.change.ChangeMetaData;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AlterTableStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AlterTableChange
 */
@DisplayName("AlterTableChange")
public class AlterTableChangeTest {
    
    @Test
    @DisplayName("Should set and get all basic properties correctly")
    public void testBasicProperties() {
        AlterTableChange change = new AlterTableChange();
        
        // Test required property
        assertNull(change.getTableName());
        change.setTableName("TEST_TABLE");
        assertEquals("TEST_TABLE", change.getTableName());
        
        // Test catalog and schema
        assertNull(change.getCatalogName());
        change.setCatalogName("TEST_CATALOG");
        assertEquals("TEST_CATALOG", change.getCatalogName());
        
        assertNull(change.getSchemaName());
        change.setSchemaName("TEST_SCHEMA");
        assertEquals("TEST_SCHEMA", change.getSchemaName());
        
        // Test clustering properties
        assertNull(change.getClusterBy());
        change.setClusterBy("col1,col2");
        assertEquals("col1,col2", change.getClusterBy());
        
        assertNull(change.getDropClusteringKey());
        change.setDropClusteringKey(true);
        assertTrue(change.getDropClusteringKey());
        
        assertNull(change.getSuspendRecluster());
        change.setSuspendRecluster(true);
        assertTrue(change.getSuspendRecluster());
        
        assertNull(change.getResumeRecluster());
        change.setResumeRecluster(true);
        assertTrue(change.getResumeRecluster());
        
        // Test property settings
        assertNull(change.getSetDataRetentionTimeInDays());
        change.setSetDataRetentionTimeInDays(30);
        assertEquals(30, change.getSetDataRetentionTimeInDays().intValue());
        
        assertNull(change.getSetChangeTracking());
        change.setSetChangeTracking(true);
        assertTrue(change.getSetChangeTracking());
        
        assertNull(change.getSetEnableSchemaEvolution());
        change.setSetEnableSchemaEvolution(false);
        assertFalse(change.getSetEnableSchemaEvolution());
    }
    
    @Test
    @DisplayName("Should only support Snowflake database")
    public void testSupports() {
        AlterTableChange change = new AlterTableChange();
        
        assertTrue(change.supports(new SnowflakeDatabase()));
        assertFalse(change.supports(new PostgresDatabase()));
    }
    
    @Test
    @DisplayName("Should generate correct statement with clustering properties")
    public void testGenerateStatementsWithClustering() {
        AlterTableChange change = new AlterTableChange();
        change.setTableName("TEST_TABLE");
        change.setSchemaName("TEST_SCHEMA");
        change.setClusterBy("col1,col2");
        
        SqlStatement[] statements = change.generateStatements(new SnowflakeDatabase());
        
        assertEquals(1, statements.length);
        assertTrue(statements[0] instanceof AlterTableStatement);
        
        AlterTableStatement stmt = (AlterTableStatement) statements[0];
        assertEquals("TEST_TABLE", stmt.getTableName());
        assertEquals("TEST_SCHEMA", stmt.getSchemaName());
        assertEquals("col1,col2", stmt.getClusterBy());
    }
    
    @Test
    @DisplayName("Should generate correct statement with property settings")
    public void testGenerateStatementsWithProperties() {
        AlterTableChange change = new AlterTableChange();
        change.setTableName("TEST_TABLE");
        change.setSetDataRetentionTimeInDays(30);
        change.setSetChangeTracking(true);
        change.setSetEnableSchemaEvolution(false);
        
        SqlStatement[] statements = change.generateStatements(new SnowflakeDatabase());
        
        assertEquals(1, statements.length);
        assertTrue(statements[0] instanceof AlterTableStatement);
        
        AlterTableStatement stmt = (AlterTableStatement) statements[0];
        assertEquals("TEST_TABLE", stmt.getTableName());
        assertEquals(30, stmt.getSetDataRetentionTimeInDays().intValue());
        assertTrue(stmt.getSetChangeTracking());
        assertFalse(stmt.getSetEnableSchemaEvolution());
    }
    
    @Test
    @DisplayName("Should fail validation when required tableName is missing")
    public void testValidationFailsWithoutTableName() {
        AlterTableChange change = new AlterTableChange();
        change.setClusterBy("col1,col2"); // Add valid operation
        
        ValidationErrors errors = change.validate(new SnowflakeDatabase());
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("tableName is required")));
    }
    
    @Test
    @DisplayName("Should fail validation with mutually exclusive clustering options")
    public void testValidationFailsWithMutualExclusions() {
        AlterTableChange change = new AlterTableChange();
        change.setTableName("TEST_TABLE");
        change.setClusterBy("col1,col2");
        change.setDropClusteringKey(true);
        
        ValidationErrors errors = change.validate(new SnowflakeDatabase());
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Only one clustering operation allowed")));
    }
    
    @Test
    @DisplayName("Should fail validation when no operations specified")
    public void testValidationFailsWithoutOperations() {
        AlterTableChange change = new AlterTableChange();
        change.setTableName("TEST_TABLE");
        // No operations set
        
        ValidationErrors errors = change.validate(new SnowflakeDatabase());
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("At least one Snowflake-specific operation must be specified")));
    }
    
    @Test
    @DisplayName("Should fail validation with invalid data retention time")
    public void testValidationFailsWithInvalidRetentionTime() {
        AlterTableChange change = new AlterTableChange();
        change.setTableName("TEST_TABLE");
        change.setSetDataRetentionTimeInDays(-1); // Invalid: less than 0
        
        ValidationErrors errors = change.validate(new SnowflakeDatabase());
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("setDataRetentionTimeInDays must be between 0 and 90")));
        
        // Test upper bound
        change.setSetDataRetentionTimeInDays(91); // Invalid: greater than 90
        errors = change.validate(new SnowflakeDatabase());
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("setDataRetentionTimeInDays must be between 0 and 90")));
    }
    
    @Test
    @DisplayName("Should pass validation with valid single clustering operation")
    public void testValidationPassesWithValidClustering() {
        AlterTableChange change = new AlterTableChange();
        change.setTableName("TEST_TABLE");
        change.setClusterBy("col1,col2");
        
        ValidationErrors errors = change.validate(new SnowflakeDatabase());
        
        assertFalse(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should pass validation with valid property combinations")
    public void testValidationPassesWithValidProperties() {
        AlterTableChange change = new AlterTableChange();
        change.setTableName("TEST_TABLE");
        change.setSetDataRetentionTimeInDays(30);
        change.setSetChangeTracking(true);
        change.setSetEnableSchemaEvolution(false);
        
        ValidationErrors errors = change.validate(new SnowflakeDatabase());
        
        assertFalse(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should not support rollback")
    public void testRollbackSupport() {
        AlterTableChange change = new AlterTableChange();
        change.setTableName("TEST_TABLE");
        change.setClusterBy("col1,col2");
        
        // ALTER TABLE operations are complex to rollback
        assertFalse(change.supportsRollback(new SnowflakeDatabase()));
    }
    
    @Test
    @DisplayName("Should return correct confirmation message for clustering")
    public void testConfirmationMessageClustering() {
        AlterTableChange change = new AlterTableChange();
        change.setTableName("TEST_TABLE");
        change.setClusterBy("col1,col2");
        
        String message = change.getConfirmationMessage();
        
        assertTrue(message.contains("Table TEST_TABLE altered"));
        assertTrue(message.contains("clustering set to (col1,col2)"));
    }
    
    @Test
    @DisplayName("Should return correct confirmation message for drop clustering")
    public void testConfirmationMessageDropClustering() {
        AlterTableChange change = new AlterTableChange();
        change.setTableName("TEST_TABLE");
        change.setDropClusteringKey(true);
        
        String message = change.getConfirmationMessage();
        
        assertTrue(message.contains("Table TEST_TABLE altered"));
        assertTrue(message.contains("clustering key dropped"));
    }
    
    @Test
    @DisplayName("Should return correct confirmation message for properties")
    public void testConfirmationMessageProperties() {
        AlterTableChange change = new AlterTableChange();
        change.setTableName("TEST_TABLE");
        change.setSetDataRetentionTimeInDays(30);
        change.setSetChangeTracking(true);
        change.setSetEnableSchemaEvolution(false);
        
        String message = change.getConfirmationMessage();
        
        assertTrue(message.contains("Table TEST_TABLE altered"));
        assertTrue(message.contains("data retention set to 30 days"));
        assertTrue(message.contains("change tracking enabled"));
        assertTrue(message.contains("schema evolution disabled"));
    }
    
    @Test
    @DisplayName("Should return correct serialized namespace")
    public void testSerializedNamespace() {
        AlterTableChange change = new AlterTableChange();
        
        assertEquals("http://www.liquibase.org/xml/ns/snowflake", 
                     change.getSerializedObjectNamespace());
    }
    
    @Test
    @DisplayName("Should be registered in ChangeFactory")
    public void testChangeRegistration() {
        ChangeMetaData metadata = ChangeFactory.getInstance()
            .getChangeMetaData("alterTable");
        
        assertNotNull(metadata);
        assertEquals("alterTable", metadata.getName());
        assertEquals("Alter table properties in Snowflake", metadata.getDescription());
    }
}