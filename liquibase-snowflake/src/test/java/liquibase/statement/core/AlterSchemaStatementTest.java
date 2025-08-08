package liquibase.statement.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AlterSchemaStatement
 */
@DisplayName("AlterSchemaStatement")
public class AlterSchemaStatementTest {
    
    private AlterSchemaStatement statement;
    
    @BeforeEach
    void setUp() {
        statement = new AlterSchemaStatement();
    }
    
    // ==================== Basic Property Tests ====================
    
    @Test
    @DisplayName("Should have correct initial state")
    void shouldHaveCorrectInitialState() {
        assertNull(statement.getSchemaName());
        assertNull(statement.getCatalogName());
        assertNull(statement.getIfExists());
        assertNull(statement.getNewName());
        assertNull(statement.getOperationType());
        assertNull(statement.getNewDataRetentionTimeInDays());
        assertNull(statement.getNewComment());
        assertNull(statement.getEnableManagedAccess());
        assertNull(statement.getDisableManagedAccess());
    }
    
    @Test
    @DisplayName("Should set and get basic properties correctly")
    void shouldSetAndGetBasicPropertiesCorrectly() {
        statement.setSchemaName("TEST_SCHEMA");
        assertEquals("TEST_SCHEMA", statement.getSchemaName());
        
        statement.setCatalogName("TEST_CATALOG");
        assertEquals("TEST_CATALOG", statement.getCatalogName());
        
        statement.setIfExists(true);
        assertEquals(true, statement.getIfExists());
        
        statement.setNewName("NEW_SCHEMA");
        assertEquals("NEW_SCHEMA", statement.getNewName());
    }
    
    @Test
    @DisplayName("Should set and get operation type correctly")
    void shouldSetAndGetOperationTypeCorrectly() {
        statement.setOperationType(AlterSchemaStatement.OperationType.RENAME);
        assertEquals(AlterSchemaStatement.OperationType.RENAME, statement.getOperationType());
        
        statement.setOperationType(AlterSchemaStatement.OperationType.SET);
        assertEquals(AlterSchemaStatement.OperationType.SET, statement.getOperationType());
        
        statement.setOperationType(AlterSchemaStatement.OperationType.UNSET);
        assertEquals(AlterSchemaStatement.OperationType.UNSET, statement.getOperationType());
        
        statement.setOperationType(AlterSchemaStatement.OperationType.ENABLE_MANAGED_ACCESS);
        assertEquals(AlterSchemaStatement.OperationType.ENABLE_MANAGED_ACCESS, statement.getOperationType());
        
        statement.setOperationType(AlterSchemaStatement.OperationType.DISABLE_MANAGED_ACCESS);
        assertEquals(AlterSchemaStatement.OperationType.DISABLE_MANAGED_ACCESS, statement.getOperationType());
    }
    
    @Test
    @DisplayName("Should set and get SET properties correctly")
    void shouldSetAndGetSetPropertiesCorrectly() {
        statement.setNewDataRetentionTimeInDays("30");
        assertEquals("30", statement.getNewDataRetentionTimeInDays());
        
        statement.setNewMaxDataExtensionTimeInDays("7");
        assertEquals("7", statement.getNewMaxDataExtensionTimeInDays());
        
        statement.setNewDefaultDdlCollation("en-US");
        assertEquals("en-US", statement.getNewDefaultDdlCollation());
        
        statement.setNewComment("Test comment");
        assertEquals("Test comment", statement.getNewComment());
        
        statement.setDropComment(true);
        assertEquals(true, statement.getDropComment());
        
        statement.setNewPipeExecutionPaused("TRUE");
        assertEquals("TRUE", statement.getNewPipeExecutionPaused());
    }
    
    @Test
    @DisplayName("Should set and get UNSET properties correctly")
    void shouldSetAndGetUnsetPropertiesCorrectly() {
        statement.setUnsetDataRetentionTimeInDays(true);
        assertEquals(true, statement.getUnsetDataRetentionTimeInDays());
        
        statement.setUnsetMaxDataExtensionTimeInDays(true);
        assertEquals(true, statement.getUnsetMaxDataExtensionTimeInDays());
        
        statement.setUnsetDefaultDdlCollation(true);
        assertEquals(true, statement.getUnsetDefaultDdlCollation());
        
        statement.setUnsetPipeExecutionPaused(true);
        assertEquals(true, statement.getUnsetPipeExecutionPaused());
        
        statement.setUnsetComment(true);
        assertEquals(true, statement.getUnsetComment());
    }
    
    @Test
    @DisplayName("Should set and get managed access properties correctly")
    void shouldSetAndGetManagedAccessPropertiesCorrectly() {
        statement.setEnableManagedAccess(true);
        assertEquals(true, statement.getEnableManagedAccess());
        
        statement.setDisableManagedAccess(true);
        assertEquals(true, statement.getDisableManagedAccess());
    }
    
    // ==================== Validation Tests ====================
    
    @Test
    @DisplayName("Should pass validation with valid schema name and RENAME operation")
    void shouldPassValidationWithValidSchemaNameAndRenameOperation() {
        statement.setSchemaName("VALID_SCHEMA");
        statement.setNewName("NEW_SCHEMA");
        
        AlterSchemaStatement.ValidationResult result = statement.validate();
        
        assertFalse(result.hasErrors());
        assertEquals(AlterSchemaStatement.OperationType.RENAME, statement.getOperationType());
    }
    
    @Test
    @DisplayName("Should fail validation when schema name is null")
    void shouldFailValidationWhenSchemaNameIsNull() {
        statement.setSchemaName(null);
        statement.setNewName("NEW_SCHEMA");
        
        AlterSchemaStatement.ValidationResult result = statement.validate();
        
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("Schema name is required")));
    }
    
    @Test
    @DisplayName("Should fail validation when schema name is empty")
    void shouldFailValidationWhenSchemaNameIsEmpty() {
        statement.setSchemaName("");
        statement.setNewName("NEW_SCHEMA");
        
        AlterSchemaStatement.ValidationResult result = statement.validate();
        
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("Schema name is required")));
    }
    
    @Test
    @DisplayName("Should fail validation when schema name has invalid format")
    void shouldFailValidationWhenSchemaNameHasInvalidFormat() {
        statement.setSchemaName("123_INVALID");
        statement.setNewName("NEW_SCHEMA");
        
        AlterSchemaStatement.ValidationResult result = statement.validate();
        
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("Invalid schema name format")));
    }
    
    @Test
    @DisplayName("Should fail validation when operation type cannot be inferred")
    void shouldFailValidationWhenOperationTypeCannotBeInferred() {
        statement.setSchemaName("TEST_SCHEMA");
        // Don't set any operation properties
        
        AlterSchemaStatement.ValidationResult result = statement.validate();
        
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("Operation type must be specified or inferable from properties")));
    }
    
    @Test
    @DisplayName("Should fail RENAME validation when new name is null")
    void shouldFailRenameValidationWhenNewNameIsNull() {
        statement.setSchemaName("TEST_SCHEMA");
        statement.setOperationType(AlterSchemaStatement.OperationType.RENAME);
        statement.setNewName(null);
        
        AlterSchemaStatement.ValidationResult result = statement.validate();
        
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("New schema name is required for RENAME operation")));
    }
    
    @Test
    @DisplayName("Should fail RENAME validation when new name has invalid format")
    void shouldFailRenameValidationWhenNewNameHasInvalidFormat() {
        statement.setSchemaName("TEST_SCHEMA");
        statement.setOperationType(AlterSchemaStatement.OperationType.RENAME);
        statement.setNewName("123_INVALID");
        
        AlterSchemaStatement.ValidationResult result = statement.validate();
        
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("Invalid new schema name format")));
    }
    
    @Test
    @DisplayName("Should fail SET validation when no SET properties specified")
    void shouldFailSetValidationWhenNoSetPropertiesSpecified() {
        statement.setSchemaName("TEST_SCHEMA");
        statement.setOperationType(AlterSchemaStatement.OperationType.SET);
        
        AlterSchemaStatement.ValidationResult result = statement.validate();
        
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("At least one property must be specified for SET operation")));
    }
    
    @Test
    @DisplayName("Should fail UNSET validation when no UNSET properties specified")
    void shouldFailUnsetValidationWhenNoUnsetPropertiesSpecified() {
        statement.setSchemaName("TEST_SCHEMA");
        statement.setOperationType(AlterSchemaStatement.OperationType.UNSET);
        
        AlterSchemaStatement.ValidationResult result = statement.validate();
        
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("At least one property must be specified for UNSET operation")));
    }
    
    @Test
    @DisplayName("Should fail validation when data retention time is negative")
    void shouldFailValidationWhenDataRetentionTimeIsNegative() {
        statement.setSchemaName("TEST_SCHEMA");
        statement.setNewDataRetentionTimeInDays("-1");
        
        AlterSchemaStatement.ValidationResult result = statement.validate();
        
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("DATA_RETENTION_TIME_IN_DAYS cannot be negative")));
    }
    
    @Test
    @DisplayName("Should fail validation when data retention time is not a valid integer")
    void shouldFailValidationWhenDataRetentionTimeIsNotValidInteger() {
        statement.setSchemaName("TEST_SCHEMA");
        statement.setNewDataRetentionTimeInDays("invalid");
        
        AlterSchemaStatement.ValidationResult result = statement.validate();
        
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("DATA_RETENTION_TIME_IN_DAYS must be a valid integer")));
    }
    
    @Test
    @DisplayName("Should generate warning when data retention time exceeds 90 days")
    void shouldGenerateWarningWhenDataRetentionTimeExceeds90Days() {
        statement.setSchemaName("TEST_SCHEMA");
        statement.setNewDataRetentionTimeInDays("120");
        
        AlterSchemaStatement.ValidationResult result = statement.validate();
        
        assertTrue(result.hasWarnings());
        assertTrue(result.getWarnings().stream()
            .anyMatch(warning -> warning.contains("DATA_RETENTION_TIME_IN_DAYS > 90 days requires Snowflake Enterprise Edition")));
    }
    
    @Test
    @DisplayName("Should fail validation when data retention time exceeds 365 days")
    void shouldFailValidationWhenDataRetentionTimeExceeds365Days() {
        statement.setSchemaName("TEST_SCHEMA");
        statement.setNewDataRetentionTimeInDays("400");
        
        AlterSchemaStatement.ValidationResult result = statement.validate();
        
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("DATA_RETENTION_TIME_IN_DAYS cannot exceed 365 days")));
    }
    
    @Test
    @DisplayName("Should fail validation when max data extension time exceeds 14 days")
    void shouldFailValidationWhenMaxDataExtensionTimeExceeds14Days() {
        statement.setSchemaName("TEST_SCHEMA");
        statement.setNewMaxDataExtensionTimeInDays("15");
        
        AlterSchemaStatement.ValidationResult result = statement.validate();
        
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("MAX_DATA_EXTENSION_TIME_IN_DAYS cannot exceed 14 days")));
    }
    
    @Test
    @DisplayName("Should fail validation when pipe execution paused has invalid value")
    void shouldFailValidationWhenPipeExecutionPausedHasInvalidValue() {
        statement.setSchemaName("TEST_SCHEMA");
        statement.setNewPipeExecutionPaused("INVALID");
        
        AlterSchemaStatement.ValidationResult result = statement.validate();
        
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("Invalid PIPE_EXECUTION_PAUSED value")));
    }
    
    @Test
    @DisplayName("Should generate warning for ENABLE MANAGED ACCESS operation")
    void shouldGenerateWarningForEnableManagedAccessOperation() {
        statement.setSchemaName("TEST_SCHEMA");
        statement.setEnableManagedAccess(true);
        
        AlterSchemaStatement.ValidationResult result = statement.validate();
        
        assertTrue(result.hasWarnings());
        assertTrue(result.getWarnings().stream()
            .anyMatch(warning -> warning.contains("Managed access schemas require Snowflake Enterprise Edition")));
    }
    
    @Test
    @DisplayName("Should automatically infer operation type from properties")
    void shouldAutomaticallyInferOperationTypeFromProperties() {
        // Test RENAME inference
        statement.setSchemaName("TEST_SCHEMA");
        statement.setNewName("NEW_SCHEMA");
        
        AlterSchemaStatement.ValidationResult result = statement.validate();
        
        assertFalse(result.hasErrors());
        assertEquals(AlterSchemaStatement.OperationType.RENAME, statement.getOperationType());
        
        // Test SET inference
        statement = new AlterSchemaStatement();
        statement.setSchemaName("TEST_SCHEMA");
        statement.setNewComment("Test comment");
        
        result = statement.validate();
        
        assertFalse(result.hasErrors());
        assertEquals(AlterSchemaStatement.OperationType.SET, statement.getOperationType());
        
        // Test UNSET inference
        statement = new AlterSchemaStatement();
        statement.setSchemaName("TEST_SCHEMA");
        statement.setUnsetComment(true);
        
        result = statement.validate();
        
        assertFalse(result.hasErrors());
        assertEquals(AlterSchemaStatement.OperationType.UNSET, statement.getOperationType());
        
        // Test ENABLE_MANAGED_ACCESS inference
        statement = new AlterSchemaStatement();
        statement.setSchemaName("TEST_SCHEMA");
        statement.setEnableManagedAccess(true);
        
        result = statement.validate();
        
        assertTrue(result.hasWarnings()); // Should have Enterprise Edition warning
        assertEquals(AlterSchemaStatement.OperationType.ENABLE_MANAGED_ACCESS, statement.getOperationType());
        
        // Test DISABLE_MANAGED_ACCESS inference
        statement = new AlterSchemaStatement();
        statement.setSchemaName("TEST_SCHEMA");
        statement.setDisableManagedAccess(true);
        
        result = statement.validate();
        
        assertFalse(result.hasErrors());
        assertEquals(AlterSchemaStatement.OperationType.DISABLE_MANAGED_ACCESS, statement.getOperationType());
    }
    
    @Test
    @DisplayName("Should accept valid schema names with underscores and dollar signs")
    void shouldAcceptValidSchemaNamesWithUnderscoresAndDollarSigns() {
        String[] validNames = {"TEST_SCHEMA", "SCHEMA_WITH_UNDERSCORE", "SCHEMA$WITH$DOLLAR", "MY_TEST$SCHEMA_123"};
        
        for (String name : validNames) {
            statement.setSchemaName(name);
            statement.setNewComment("Test comment");
            
            AlterSchemaStatement.ValidationResult result = statement.validate();
            
            assertFalse(result.hasErrors(), "Schema name '" + name + "' should be valid");
        }
    }
    
    @Test
    @DisplayName("Should accept valid pipe execution paused values")
    void shouldAcceptValidPipeExecutionPausedValues() {
        String[] validValues = {"TRUE", "FALSE", "true", "false", "True", "False"};
        
        for (String value : validValues) {
            statement = new AlterSchemaStatement();
            statement.setSchemaName("TEST_SCHEMA");
            statement.setNewPipeExecutionPaused(value);
            
            AlterSchemaStatement.ValidationResult result = statement.validate();
            
            assertFalse(result.hasErrors(), "Pipe execution paused value '" + value + "' should be valid");
        }
    }
}