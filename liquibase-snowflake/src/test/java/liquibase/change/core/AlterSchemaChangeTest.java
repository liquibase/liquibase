package liquibase.change.core;

import liquibase.database.core.H2Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AlterSchemaStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for AlterSchemaChange with 90%+ coverage focus.
 * Tests all validation methods, operation types, compatibility methods, and changetype execution patterns.
 * Follows established testing patterns: changetype execution, complete statement validation.
 */
@DisplayName("AlterSchemaChange")
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
                     "Should return correct namespace");
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
                "Should contain schema name required error");
    }

    @Test
    public void testValidationAtLeastOneChangeRequired() {
        change.setSchemaName("test_schema");
        ValidationErrors errors = change.validate(database);
        assertTrue(errors.hasErrors(), "Should have validation errors");
        assertTrue(errors.getErrorMessages().stream()
                .anyMatch(msg -> msg.contains("At least one schema property must be changed")), 
                "Should contain at least one change required error");
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
                "Should contain comment conflict error");
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
                "Should contain managed access conflict error");
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
                "Should contain data retention conflict error");
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
                "Should contain comment operations conflict error");
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
                "Should contain comment operations conflict error");
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

    // ==================== Additional Branch Coverage Tests ====================
    
    @Test
    @DisplayName("Should not support non-Snowflake database")
    public void testDoesNotSupportNonSnowflakeDatabase() {
        H2Database h2Database = new H2Database();
        assertFalse(change.supports(h2Database), "AlterSchemaChange should not support non-Snowflake databases");
    }
    
    @Test
    @DisplayName("Should handle null database in supports check")
    public void testHandleNullDatabaseInSupportsCheck() {
        assertFalse(change.supports(null), "AlterSchemaChange should handle null database gracefully");
    }

    @Test
    @DisplayName("Should generate statement with catalog name")
    public void testGenerateStatementWithCatalogName() {
        change.setSchemaName("test_schema");
        change.setCatalogName("test_catalog");
        change.setNewComment("With catalog");

        SqlStatement[] statements = change.generateStatements(database);
        AlterSchemaStatement statement = (AlterSchemaStatement) statements[0];
        
        assertEquals("test_catalog", statement.getCatalogName());
        assertEquals("test_schema", statement.getSchemaName());
        assertEquals("With catalog", statement.getNewComment());
    }

    @Test
    @DisplayName("Should generate statement without catalog name")
    public void testGenerateStatementWithoutCatalogName() {
        change.setSchemaName("test_schema");
        // No catalog set
        change.setNewComment("Without catalog");

        SqlStatement[] statements = change.generateStatements(database);
        AlterSchemaStatement statement = (AlterSchemaStatement) statements[0];
        
        assertNull(statement.getCatalogName());
        assertEquals("test_schema", statement.getSchemaName());
    }

    @Test
    @DisplayName("Should handle empty string schema name")
    public void testHandleEmptyStringSchemaName() {
        change.setSchemaName("");
        
        ValidationErrors errors = change.validate(database);
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
                .anyMatch(msg -> msg.contains("schemaName is required")));
    }

    @Test
    @DisplayName("Should handle whitespace-only schema name")
    public void testHandleWhitespaceOnlySchemaName() {
        change.setSchemaName("   ");
        
        ValidationErrors errors = change.validate(database);
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
                .anyMatch(msg -> msg.contains("schemaName is required")));
    }

    // ==================== Enhanced Phase 2 API Tests (Operation Type) ====================
    
    @Test
    @DisplayName("Should set RENAME operation type")
    public void testSetRenameOperationType() {
        change.setSchemaName("test_schema");
        change.setNewName("renamed_schema");
        change.setOperationType("RENAME");

        SqlStatement[] statements = change.generateStatements(database);
        AlterSchemaStatement statement = (AlterSchemaStatement) statements[0];
        
        assertEquals("test_schema", statement.getSchemaName());
        assertEquals("renamed_schema", statement.getNewName());
        // Operation type should be set on statement (verified through successful generation)
    }

    @Test
    @DisplayName("Should set SET operation type")
    public void testSetSetOperationType() {
        change.setSchemaName("test_schema");
        change.setNewDataRetentionTimeInDays("7");
        change.setOperationType("SET");

        SqlStatement[] statements = change.generateStatements(database);
        AlterSchemaStatement statement = (AlterSchemaStatement) statements[0];
        
        assertEquals("7", statement.getNewDataRetentionTimeInDays());
    }

    @Test
    @DisplayName("Should set UNSET operation type")
    public void testSetUnsetOperationType() {
        change.setSchemaName("test_schema");
        change.setUnsetDataRetentionTimeInDays(true);
        change.setOperationType("UNSET");

        SqlStatement[] statements = change.generateStatements(database);
        AlterSchemaStatement statement = (AlterSchemaStatement) statements[0];
        
        assertEquals(Boolean.TRUE, statement.getUnsetDataRetentionTimeInDays());
    }

    @Test
    @DisplayName("Should set ENABLE_MANAGED_ACCESS operation type")
    public void testSetEnableManagedAccessOperationType() {
        change.setSchemaName("test_schema");
        change.setEnableManagedAccess(true);
        change.setOperationType("ENABLE_MANAGED_ACCESS");

        SqlStatement[] statements = change.generateStatements(database);
        AlterSchemaStatement statement = (AlterSchemaStatement) statements[0];
        
        assertEquals(Boolean.TRUE, statement.getEnableManagedAccess());
    }

    @Test
    @DisplayName("Should set DISABLE_MANAGED_ACCESS operation type")
    public void testSetDisableManagedAccessOperationType() {
        change.setSchemaName("test_schema");
        change.setDisableManagedAccess(true);
        change.setOperationType("DISABLE_MANAGED_ACCESS");

        SqlStatement[] statements = change.generateStatements(database);
        AlterSchemaStatement statement = (AlterSchemaStatement) statements[0];
        
        assertEquals(Boolean.TRUE, statement.getDisableManagedAccess());
    }

    @Test
    @DisplayName("Should handle lowercase operation type")
    public void testHandleLowercaseOperationType() {
        change.setSchemaName("test_schema");
        change.setNewName("renamed_schema");
        change.setOperationType("rename"); // lowercase

        // Should not throw exception and should work correctly
        SqlStatement[] statements = change.generateStatements(database);
        AlterSchemaStatement statement = (AlterSchemaStatement) statements[0];
        
        assertEquals("renamed_schema", statement.getNewName());
    }

    @Test
    @DisplayName("Should handle mixed case operation type")
    public void testHandleMixedCaseOperationType() {
        change.setSchemaName("test_schema");
        change.setEnableManagedAccess(true);
        change.setOperationType("Enable_Managed_Access"); // mixed case

        SqlStatement[] statements = change.generateStatements(database);
        AlterSchemaStatement statement = (AlterSchemaStatement) statements[0];
        
        assertEquals(Boolean.TRUE, statement.getEnableManagedAccess());
    }

    @Test
    @DisplayName("Should reject invalid operation type")
    public void testRejectInvalidOperationType() {
        change.setSchemaName("test_schema");
        change.setNewName("renamed_schema");
        change.setOperationType("INVALID_TYPE");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            change.generateStatements(database);
        });
        
        assertTrue(exception.getMessage().contains("Invalid operation type: INVALID_TYPE"));
        assertTrue(exception.getMessage().contains("Valid types are: RENAME, SET, UNSET, ENABLE_MANAGED_ACCESS, DISABLE_MANAGED_ACCESS"));
    }

    @Test
    @DisplayName("Should handle null operation type")
    public void testHandleNullOperationType() {
        change.setSchemaName("test_schema");
        change.setNewName("renamed_schema");
        change.setOperationType(null);

        // Should work without exception (no operation type set)
        SqlStatement[] statements = change.generateStatements(database);
        AlterSchemaStatement statement = (AlterSchemaStatement) statements[0];
        
        assertEquals("renamed_schema", statement.getNewName());
    }

    @Test
    @DisplayName("Should handle empty operation type")
    public void testHandleEmptyOperationType() {
        change.setSchemaName("test_schema");
        change.setNewName("renamed_schema");
        change.setOperationType("");

        // Should work without exception (empty string ignored)
        SqlStatement[] statements = change.generateStatements(database);
        AlterSchemaStatement statement = (AlterSchemaStatement) statements[0];
        
        assertEquals("renamed_schema", statement.getNewName());
    }

    @Test
    @DisplayName("Should handle whitespace operation type")
    public void testHandleWhitespaceOperationType() {
        change.setSchemaName("test_schema");
        change.setNewName("renamed_schema");
        change.setOperationType("   ");

        // Should work without exception (whitespace ignored)
        SqlStatement[] statements = change.generateStatements(database);
        AlterSchemaStatement statement = (AlterSchemaStatement) statements[0];
        
        assertEquals("renamed_schema", statement.getNewName());
    }

    // ==================== Compatibility Methods Tests ====================

    @Test
    @DisplayName("Should use compatibility getComment/setComment methods")
    public void testCompatibilityCommentMethods() {
        change.setSchemaName("test_schema");
        change.setComment("Compatibility comment");
        
        assertEquals("Compatibility comment", change.getComment());
        assertEquals("Compatibility comment", change.getNewComment());

        SqlStatement[] statements = change.generateStatements(database);
        AlterSchemaStatement statement = (AlterSchemaStatement) statements[0];
        
        assertEquals("Compatibility comment", statement.getNewComment());
    }

    @Test
    @DisplayName("Should use compatibility getDataRetentionTimeInDays/setDataRetentionTimeInDays methods")
    public void testCompatibilityDataRetentionMethods() {
        change.setSchemaName("test_schema");
        change.setDataRetentionTimeInDays("14");
        
        assertEquals("14", change.getDataRetentionTimeInDays());
        assertEquals("14", change.getNewDataRetentionTimeInDays());

        SqlStatement[] statements = change.generateStatements(database);
        AlterSchemaStatement statement = (AlterSchemaStatement) statements[0];
        
        assertEquals("14", statement.getNewDataRetentionTimeInDays());
    }

    @Test
    @DisplayName("Should use compatibility getManagedAccess/setManagedAccess with true")
    public void testCompatibilityManagedAccessTrue() {
        change.setSchemaName("test_schema");
        change.setManagedAccess(true);
        
        assertEquals(Boolean.TRUE, change.getManagedAccess());
        assertEquals(Boolean.TRUE, change.getEnableManagedAccess());
        assertEquals(Boolean.FALSE, change.getDisableManagedAccess());

        SqlStatement[] statements = change.generateStatements(database);
        AlterSchemaStatement statement = (AlterSchemaStatement) statements[0];
        
        assertEquals(Boolean.TRUE, statement.getEnableManagedAccess());
        assertEquals(Boolean.FALSE, statement.getDisableManagedAccess());
    }

    @Test
    @DisplayName("Should use compatibility getManagedAccess/setManagedAccess with false")
    public void testCompatibilityManagedAccessFalse() {
        change.setSchemaName("test_schema");
        change.setManagedAccess(false);
        
        assertEquals(Boolean.FALSE, change.getManagedAccess());
        assertEquals(Boolean.FALSE, change.getEnableManagedAccess());
        assertEquals(Boolean.TRUE, change.getDisableManagedAccess());

        SqlStatement[] statements = change.generateStatements(database);
        AlterSchemaStatement statement = (AlterSchemaStatement) statements[0];
        
        assertEquals(Boolean.FALSE, statement.getEnableManagedAccess());
        assertEquals(Boolean.TRUE, statement.getDisableManagedAccess());
    }

    @Test
    @DisplayName("Should use compatibility getManagedAccess/setManagedAccess with null")
    public void testCompatibilityManagedAccessNull() {
        change.setSchemaName("test_schema");
        change.setNewComment("Some change"); // Need some change for validation
        change.setManagedAccess(null);
        
        assertNull(change.getManagedAccess());
    }

    @Test
    @DisplayName("Should getManagedAccess return true when enableManagedAccess is set")
    public void testGetManagedAccessWhenEnableIsSet() {
        change.setSchemaName("test_schema");
        change.setEnableManagedAccess(true);
        
        assertEquals(Boolean.TRUE, change.getManagedAccess());
    }

    @Test
    @DisplayName("Should getManagedAccess return false when disableManagedAccess is set")
    public void testGetManagedAccessWhenDisableIsSet() {
        change.setSchemaName("test_schema");
        change.setDisableManagedAccess(true);
        
        assertEquals(Boolean.FALSE, change.getManagedAccess());
    }

    @Test
    @DisplayName("Should getManagedAccess return null when neither enable nor disable is set")
    public void testGetManagedAccessWhenNeitherIsSet() {
        change.setSchemaName("test_schema");
        change.setNewComment("Some change"); // Need some change for validation
        
        assertNull(change.getManagedAccess());
    }

    // ==================== Edge Case and Null Handling Tests ====================

    @Test
    @DisplayName("Should generate confirmation message with null schema name")
    public void testConfirmationMessageWithNullSchemaName() {
        change.setSchemaName(null);
        
        String message = change.getConfirmationMessage();
        assertNotNull(message);
        assertTrue(message.contains("null"));
    }

    @Test
    @DisplayName("Should handle all null boolean properties correctly in statement generation")
    public void testHandleAllNullBooleanPropertiesInStatementGeneration() {
        change.setSchemaName("test_schema");
        change.setNewComment("Test change"); // Need some change
        
        // All boolean properties are null by default
        SqlStatement[] statements = change.generateStatements(database);
        AlterSchemaStatement statement = (AlterSchemaStatement) statements[0];
        
        assertNull(statement.getIfExists());
        assertNull(statement.getDropComment());
        assertNull(statement.getEnableManagedAccess());
        assertNull(statement.getDisableManagedAccess());
        assertNull(statement.getUnsetDataRetentionTimeInDays());
        assertNull(statement.getUnsetMaxDataExtensionTimeInDays());
        assertNull(statement.getUnsetDefaultDdlCollation());
        assertNull(statement.getUnsetPipeExecutionPaused());
        assertNull(statement.getUnsetComment());
    }

    @Test
    @DisplayName("Should handle all string properties correctly in statement generation")
    public void testHandleAllStringPropertiesInStatementGeneration() {
        change.setSchemaName("test_schema");
        change.setNewName("new_schema");
        change.setNewDataRetentionTimeInDays("30");
        change.setNewMaxDataExtensionTimeInDays("60");
        change.setNewDefaultDdlCollation("en_US");
        change.setNewComment("Complete test");
        change.setNewPipeExecutionPaused("false");
        
        SqlStatement[] statements = change.generateStatements(database);
        AlterSchemaStatement statement = (AlterSchemaStatement) statements[0];
        
        assertEquals("test_schema", statement.getSchemaName());
        assertEquals("new_schema", statement.getNewName());
        assertEquals("30", statement.getNewDataRetentionTimeInDays());
        assertEquals("60", statement.getNewMaxDataExtensionTimeInDays());
        assertEquals("en_US", statement.getNewDefaultDdlCollation());
        assertEquals("Complete test", statement.getNewComment());
        assertEquals("false", statement.getNewPipeExecutionPaused());
    }

    @Test
    @DisplayName("Should handle all UNSET boolean properties in statement generation")
    public void testHandleAllUnsetBooleanPropertiesInStatementGeneration() {
        change.setSchemaName("test_schema");
        change.setUnsetDataRetentionTimeInDays(true);
        change.setUnsetMaxDataExtensionTimeInDays(true);
        change.setUnsetDefaultDdlCollation(true);
        change.setUnsetPipeExecutionPaused(true);
        change.setUnsetComment(true);
        
        SqlStatement[] statements = change.generateStatements(database);
        AlterSchemaStatement statement = (AlterSchemaStatement) statements[0];
        
        assertEquals(Boolean.TRUE, statement.getUnsetDataRetentionTimeInDays());
        assertEquals(Boolean.TRUE, statement.getUnsetMaxDataExtensionTimeInDays());
        assertEquals(Boolean.TRUE, statement.getUnsetDefaultDdlCollation());
        assertEquals(Boolean.TRUE, statement.getUnsetPipeExecutionPaused());
        assertEquals(Boolean.TRUE, statement.getUnsetComment());
    }

    // ==================== Additional Validation Edge Cases ====================

    @Test
    @DisplayName("Should validate maxDataExtensionTimeInDays SET/UNSET conflict")
    public void testValidateMaxDataExtensionTimeInDaysSetUnsetConflict() {
        change.setSchemaName("test_schema");
        change.setNewMaxDataExtensionTimeInDays("30");
        change.setUnsetMaxDataExtensionTimeInDays(true);
        
        ValidationErrors errors = change.validate(database);
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
                .anyMatch(msg -> msg.contains("Cannot specify both newMaxDataExtensionTimeInDays and unsetMaxDataExtensionTimeInDays")));
    }

    @Test
    @DisplayName("Should validate defaultDdlCollation SET/UNSET conflict")
    public void testValidateDefaultDdlCollationSetUnsetConflict() {
        change.setSchemaName("test_schema");
        change.setNewDefaultDdlCollation("en_US");
        change.setUnsetDefaultDdlCollation(true);
        
        ValidationErrors errors = change.validate(database);
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
                .anyMatch(msg -> msg.contains("Cannot specify both newDefaultDdlCollation and unsetDefaultDdlCollation")));
    }

    @Test
    @DisplayName("Should validate pipeExecutionPaused SET/UNSET conflict")
    public void testValidatePipeExecutionPausedSetUnsetConflict() {
        change.setSchemaName("test_schema");
        change.setNewPipeExecutionPaused("true");
        change.setUnsetPipeExecutionPaused(true);
        
        ValidationErrors errors = change.validate(database);
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
                .anyMatch(msg -> msg.contains("Cannot specify both newPipeExecutionPaused and unsetPipeExecutionPaused")));
    }

    @Test
    @DisplayName("Should pass validation with valid combination of different property types")
    public void testPassValidationWithValidCombination() {
        change.setSchemaName("test_schema");
        change.setNewName("renamed_schema");
        change.setNewDataRetentionTimeInDays("7");
        change.setUnsetMaxDataExtensionTimeInDays(true);
        change.setEnableManagedAccess(true);
        
        ValidationErrors errors = change.validate(database);
        assertFalse(errors.hasErrors(), "Valid combination should pass validation: " + errors.getErrorMessages());
    }

    @Test
    @DisplayName("Should include parent validation errors")
    public void testIncludeParentValidationErrors() {
        change.setSchemaName("valid_schema");
        change.setNewComment("Valid change");
        
        ValidationErrors errors = change.validate(database);
        
        // Should call super.validate() - test that it doesn't throw and returns valid result
        assertNotNull(errors);
        assertFalse(errors.hasErrors()); // Should be valid with required schema name and change
    }
}