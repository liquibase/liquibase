package liquibase.change.core;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AlterSchemaStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AlterSchemaChange
 */
public class AlterSchemaChangeTest {

    private AlterSchemaChange change;
    private SnowflakeDatabase database;

    @BeforeEach
    public void setUp() {
        change = new AlterSchemaChange();
        database = new SnowflakeDatabase();
    }

    @Test
    public void testSupportsSnowflake() {
        assertTrue(change.supports(database), "AlterSchemaChange should support Snowflake database");
    }

    @Test
    public void testDoesNotSupportRollback() {
        assertFalse(change.supportsRollback(database), "AlterSchemaChange should not support rollback");
    }

    @Test
    public void testGetSerializedObjectNamespace() {
        assertEquals("http://www.liquibase.org/xml/ns/snowflake", 
                     change.getSerializedObjectNamespace(),
                     "AlterSchemaChange should return correct namespace");
    }

    @Test
    public void testBasicRename() {
        change.setSchemaName("old_schema");
        change.setNewName("new_schema");

        SqlStatement[] statements = change.generateStatements(database);
        assertEquals(1, statements.length, "Should generate one statement for rename");
        
        AlterSchemaStatement statement = (AlterSchemaStatement) statements[0];
        assertEquals("old_schema", statement.getSchemaName());
        assertEquals("new_schema", statement.getNewName());
        assertNull(statement.getIfExists());
    }

    @Test
    public void testRenameWithIfExists() {
        change.setSchemaName("old_schema");
        change.setNewName("new_schema");
        change.setIfExists(true);

        SqlStatement[] statements = change.generateStatements(database);
        AlterSchemaStatement statement = (AlterSchemaStatement) statements[0];
        assertEquals(true, statement.getIfExists());
    }

    @Test
    public void testSetProperties() {
        change.setSchemaName("test_schema");
        change.setNewDataRetentionTimeInDays("7");
        change.setNewMaxDataExtensionTimeInDays("14");
        change.setNewDefaultDdlCollation("utf8");
        change.setNewComment("Updated schema");
        change.setNewPipeExecutionPaused("true");

        SqlStatement[] statements = change.generateStatements(database);
        AlterSchemaStatement statement = (AlterSchemaStatement) statements[0];
        
        assertEquals("test_schema", statement.getSchemaName());
        assertEquals("7", statement.getNewDataRetentionTimeInDays());
        assertEquals("14", statement.getNewMaxDataExtensionTimeInDays());
        assertEquals("utf8", statement.getNewDefaultDdlCollation());
        assertEquals("Updated schema", statement.getNewComment());
        assertEquals("true", statement.getNewPipeExecutionPaused());
    }

    @Test
    public void testUnsetProperties() {
        change.setSchemaName("test_schema");
        change.setUnsetDataRetentionTimeInDays(true);
        change.setUnsetMaxDataExtensionTimeInDays(true);
        change.setUnsetDefaultDdlCollation(true);
        change.setUnsetPipeExecutionPaused(true);
        change.setUnsetComment(true);

        SqlStatement[] statements = change.generateStatements(database);
        AlterSchemaStatement statement = (AlterSchemaStatement) statements[0];
        
        assertEquals(true, statement.getUnsetDataRetentionTimeInDays());
        assertEquals(true, statement.getUnsetMaxDataExtensionTimeInDays());
        assertEquals(true, statement.getUnsetDefaultDdlCollation());
        assertEquals(true, statement.getUnsetPipeExecutionPaused());
        assertEquals(true, statement.getUnsetComment());
    }

    @Test
    public void testManagedAccess() {
        change.setSchemaName("test_schema");
        change.setEnableManagedAccess(true);

        SqlStatement[] statements = change.generateStatements(database);
        AlterSchemaStatement statement = (AlterSchemaStatement) statements[0];
        assertEquals(true, statement.getEnableManagedAccess());

        // Test disable
        change.setEnableManagedAccess(null);
        change.setDisableManagedAccess(true);
        
        statements = change.generateStatements(database);
        statement = (AlterSchemaStatement) statements[0];
        assertEquals(true, statement.getDisableManagedAccess());
    }

    @Test
    public void testDropComment() {
        change.setSchemaName("test_schema");
        change.setDropComment(true);

        SqlStatement[] statements = change.generateStatements(database);
        AlterSchemaStatement statement = (AlterSchemaStatement) statements[0];
        assertEquals(true, statement.getDropComment());
    }

    @Test
    public void testConfirmationMessage() {
        change.setSchemaName("test_schema");
        assertEquals("Schema test_schema altered", change.getConfirmationMessage());
    }

    // Validation Tests
    @Test
    public void testValidationSchemaNameRequired() {
        ValidationErrors errors = change.validate(database);
        assertTrue(errors.hasErrors(), "Should have validation errors");
        assertTrue(errors.getErrorMessages().stream()
                .anyMatch(msg -> msg.contains("schemaName is required")), 
                "Should require schemaName");
    }

    @Test
    public void testValidationAtLeastOneChangeRequired() {
        change.setSchemaName("test_schema");
        ValidationErrors errors = change.validate(database);
        assertTrue(errors.hasErrors(), "Should have validation errors");
        assertTrue(errors.getErrorMessages().stream()
                .anyMatch(msg -> msg.contains("At least one schema property must be changed")), 
                "Should require at least one change");
    }

    @Test
    public void testValidationCannotSetAndDropComment() {
        change.setSchemaName("test_schema");
        change.setNewComment("New comment");
        change.setDropComment(true);

        ValidationErrors errors = change.validate(database);
        assertTrue(errors.hasErrors(), "Should have validation errors");
        assertTrue(errors.getErrorMessages().stream()
                .anyMatch(msg -> msg.contains("Cannot specify both newComment and dropComment")), 
                "Should reject both newComment and dropComment");
    }

    @Test
    public void testValidationCannotEnableAndDisableManagedAccess() {
        change.setSchemaName("test_schema");
        change.setEnableManagedAccess(true);
        change.setDisableManagedAccess(true);

        ValidationErrors errors = change.validate(database);
        assertTrue(errors.hasErrors(), "Should have validation errors");
        assertTrue(errors.getErrorMessages().stream()
                .anyMatch(msg -> msg.contains("Cannot specify both enableManagedAccess and disableManagedAccess")), 
                "Should reject both enable and disable managed access");
    }

    @Test
    public void testValidationCannotSetAndUnsetSameProperty() {
        change.setSchemaName("test_schema");
        change.setNewDataRetentionTimeInDays("7");
        change.setUnsetDataRetentionTimeInDays(true);

        ValidationErrors errors = change.validate(database);
        assertTrue(errors.hasErrors(), "Should have validation errors");
        assertTrue(errors.getErrorMessages().stream()
                .anyMatch(msg -> msg.contains("Cannot specify both newDataRetentionTimeInDays and unsetDataRetentionTimeInDays")), 
                "Should reject both set and unset for same property");
    }

    @Test
    public void testValidationCannotSetCommentAndUnsetComment() {
        change.setSchemaName("test_schema");
        change.setNewComment("Test comment");
        change.setUnsetComment(true);

        ValidationErrors errors = change.validate(database);
        assertTrue(errors.hasErrors(), "Should have validation errors");
        assertTrue(errors.getErrorMessages().stream()
                .anyMatch(msg -> msg.contains("Cannot specify comment operations (set/drop) with unsetComment")), 
                "Should reject set comment with unset comment");
    }

    @Test
    public void testValidationCannotDropCommentAndUnsetComment() {
        change.setSchemaName("test_schema");
        change.setDropComment(true);
        change.setUnsetComment(true);

        ValidationErrors errors = change.validate(database);
        assertTrue(errors.hasErrors(), "Should have validation errors");
        assertTrue(errors.getErrorMessages().stream()
                .anyMatch(msg -> msg.contains("Cannot specify comment operations (set/drop) with unsetComment")), 
                "Should reject drop comment with unset comment");
    }

    @Test
    public void testValidRenameOperation() {
        change.setSchemaName("test_schema");
        change.setNewName("renamed_schema");

        ValidationErrors errors = change.validate(database);
        assertFalse(errors.hasErrors(), "Rename operation should be valid");
    }

    @Test
    public void testValidSetOperation() {
        change.setSchemaName("test_schema");
        change.setNewDataRetentionTimeInDays("7");

        ValidationErrors errors = change.validate(database);
        assertFalse(errors.hasErrors(), "Set operation should be valid");
    }

    @Test
    public void testValidUnsetOperation() {
        change.setSchemaName("test_schema");
        change.setUnsetDataRetentionTimeInDays(true);

        ValidationErrors errors = change.validate(database);
        assertFalse(errors.hasErrors(), "Unset operation should be valid");
    }

    @Test
    public void testValidManagedAccessOperation() {
        change.setSchemaName("test_schema");
        change.setEnableManagedAccess(true);

        ValidationErrors errors = change.validate(database);
        assertFalse(errors.hasErrors(), "Managed access operation should be valid");
    }
}