package liquibase.change.core;

import liquibase.change.Change;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropStageStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for DropStageChange with 90%+ coverage focus.
 * Tests validation, ifExists support, and simple drop operations.
 * Follows established testing patterns: changetype execution, validation testing.
 */
@DisplayName("DropStageChange")
public class DropStageChangeTest {
    
    private DropStageChange change;
    private SnowflakeDatabase database;
    
    @BeforeEach
    void setUp() {
        change = new DropStageChange();
        database = new SnowflakeDatabase();
    }
    
    // ==================== Basic Functionality Tests ====================
    
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
    @DisplayName("Should generate basic drop stage statement")
    void shouldGenerateBasicDropStageStatement() {
        // Given
        change.setStageName("TEST_STAGE");
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        assertEquals(1, statements.length);
        assertTrue(statements[0] instanceof DropStageStatement);
        
        DropStageStatement stmt = (DropStageStatement) statements[0];
        assertEquals("TEST_STAGE", stmt.getStageName());
        assertNull(stmt.getIfExists()); // Default is null (not set)
    }
    
    @Test
    @DisplayName("Should generate statement with IF EXISTS")
    void shouldGenerateStatementWithIfExists() {
        // Given
        change.setStageName("IF_EXISTS_STAGE");
        change.setIfExists(true);
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        assertEquals(1, statements.length);
        DropStageStatement stmt = (DropStageStatement) statements[0];
        
        assertEquals("IF_EXISTS_STAGE", stmt.getStageName());
        assertTrue(stmt.getIfExists());
    }
    
    @Test
    @DisplayName("Should generate statement with schema qualification")
    void shouldGenerateStatementWithSchemaQualification() {
        // Given
        change.setStageName("QUALIFIED_STAGE");
        change.setSchemaName("MY_SCHEMA");
        change.setCatalogName("MY_DATABASE");
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        assertEquals(1, statements.length);
        DropStageStatement stmt = (DropStageStatement) statements[0];
        
        assertEquals("QUALIFIED_STAGE", stmt.getStageName());
        assertEquals("MY_SCHEMA", stmt.getSchemaName());
        assertEquals("MY_DATABASE", stmt.getCatalogName());
    }
    
    @Test
    @DisplayName("Should generate statement with all properties")
    void shouldGenerateStatementWithAllProperties() {
        // Given
        change.setStageName("FULL_DROP_STAGE");
        change.setSchemaName("TEST_SCHEMA");
        change.setCatalogName("TEST_DB");
        change.setIfExists(true);
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        assertEquals(1, statements.length);
        DropStageStatement stmt = (DropStageStatement) statements[0];
        
        assertEquals("FULL_DROP_STAGE", stmt.getStageName());
        assertEquals("TEST_SCHEMA", stmt.getSchemaName());
        assertEquals("TEST_DB", stmt.getCatalogName());
        assertTrue(stmt.getIfExists());
    }
    
    // ==================== Validation Tests ====================
    
    @Test
    @DisplayName("Should require stageName")
    void shouldRequireStageName() {
        // Given - Change without required stageName
        
        // When
        ValidationErrors errors = change.validate(database);
        
        // Then
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorMessages().size());
        assertTrue(errors.getErrorMessages().get(0).contains("stageName is required"));
    }
    
    @Test
    @DisplayName("Should pass validation with valid stageName")
    void shouldPassValidationWithValidStageName() {
        // Given
        change.setStageName("VALID_STAGE");
        
        // When
        ValidationErrors errors = change.validate(database);
        
        // Then
        assertFalse(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should pass validation with all properties")
    void shouldPassValidationWithAllProperties() {
        // Given
        change.setStageName("VALID_FULL_STAGE");
        change.setSchemaName("VALID_SCHEMA");
        change.setCatalogName("VALID_DB");
        change.setIfExists(true);
        
        // When
        ValidationErrors errors = change.validate(database);
        
        // Then
        assertFalse(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should validate empty stageName as invalid")
    void shouldValidateEmptyStageNameAsInvalid() {
        // Given
        change.setStageName("");
        
        // When
        ValidationErrors errors = change.validate(database);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("stageName is required")));
    }
    
    @Test
    @DisplayName("Should validate whitespace-only stageName as invalid")
    void shouldValidateWhitespaceOnlyStageNameAsInvalid() {
        // Given
        change.setStageName("   ");
        
        // When
        ValidationErrors errors = change.validate(database);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("stageName is required")));
    }
    
    // ==================== Property Tests ====================
    
    @Test
    @DisplayName("Should handle ifExists property correctly")
    void shouldHandleIfExistsPropertyCorrectly() {
        // Test default value (null)
        assertNull(change.getIfExists());
        
        // Test setting to true
        change.setIfExists(true);
        assertTrue(change.getIfExists());
        
        // Test setting to false
        change.setIfExists(false);
        assertFalse(change.getIfExists());
        
        // Test setting to null
        change.setIfExists(null);
        assertNull(change.getIfExists());
    }
    
    @Test
    @DisplayName("Should handle schema properties correctly")
    void shouldHandleSchemaPropertiesCorrectly() {
        // Test default values
        assertNull(change.getSchemaName());
        assertNull(change.getCatalogName());
        
        // Test setting values
        change.setSchemaName("MY_SCHEMA");
        change.setCatalogName("MY_DATABASE");
        
        assertEquals("MY_SCHEMA", change.getSchemaName());
        assertEquals("MY_DATABASE", change.getCatalogName());
        
        // Test setting to null
        change.setSchemaName(null);
        change.setCatalogName(null);
        
        assertNull(change.getSchemaName());
        assertNull(change.getCatalogName());
    }
    
    // ==================== Additional Branch Coverage Tests ====================
    
    @Test
    @DisplayName("Should not support non-Snowflake database")
    void shouldNotSupportNonSnowflakeDatabase() {
        // Given
        liquibase.database.Database h2Database = org.mockito.Mockito.mock(liquibase.database.Database.class);
        
        // When/Then
        assertFalse(change.supports(h2Database));
    }
    
    @Test
    @DisplayName("Should generate confirmation message")
    void shouldGenerateConfirmationMessage() {
        // Given
        change.setStageName("TEST_STAGE");
        
        // When
        String message = change.getConfirmationMessage();
        
        // Then
        assertNotNull(message);
        assertTrue(message.contains("Stage TEST_STAGE dropped"));
    }
    
    @Test
    @DisplayName("Should generate confirmation message with IF EXISTS")
    void shouldGenerateConfirmationMessageWithIfExists() {
        // Given
        change.setStageName("IF_EXISTS_STAGE");
        change.setIfExists(true);
        
        // When
        String message = change.getConfirmationMessage();
        
        // Then
        assertNotNull(message);
        assertTrue(message.contains("Stage IF_EXISTS_STAGE dropped"));
        // The confirmation message might include IF EXISTS information
    }
    
    @Test
    @DisplayName("Should generate confirmation message with schema qualification")
    void shouldGenerateConfirmationMessageWithSchemaQualification() {
        // Given
        change.setStageName("QUALIFIED_STAGE");
        change.setSchemaName("MY_SCHEMA");
        change.setCatalogName("MY_DB");
        
        // When
        String message = change.getConfirmationMessage();
        
        // Then
        assertNotNull(message);
        assertTrue(message.contains("Stage"));
        assertTrue(message.contains("QUALIFIED_STAGE"));
        assertTrue(message.contains("dropped"));
    }
    
    @Test
    @DisplayName("Should not support rollback operations")
    void shouldNotSupportRollbackOperations() {
        // Given
        change.setStageName("TEST_STAGE");
        
        // When/Then - DropStageChange doesn't support rollback
        assertFalse(change.supportsRollback(database));
        
        // Note: createInverses() is protected and not part of public API
        // Rollback support is tested via supportsRollback() method
    }
    
    @Test
    @DisplayName("Should handle stageName getter and setter")
    void shouldHandleStageNameGetterAndSetter() {
        // Test default value
        assertNull(change.getStageName());
        
        // Test setting value
        change.setStageName("MY_STAGE");
        assertEquals("MY_STAGE", change.getStageName());
        
        // Test setting different value
        change.setStageName("ANOTHER_STAGE");
        assertEquals("ANOTHER_STAGE", change.getStageName());
        
        // Test setting to null
        change.setStageName(null);
        assertNull(change.getStageName());
    }
    
    @Test
    @DisplayName("Should handle edge cases in validation")
    void shouldHandleEdgeCasesInValidation() {
        // Test with ifExists but no stageName
        change.setIfExists(true);
        ValidationErrors errors = change.validate(database);
        assertTrue(errors.hasErrors());
        
        // Test with schema but no stageName
        change = new DropStageChange();
        change.setSchemaName("SCHEMA");
        errors = change.validate(database);
        assertTrue(errors.hasErrors());
        
        // Test with catalog but no stageName
        change = new DropStageChange();
        change.setCatalogName("DATABASE");
        errors = change.validate(database);
        assertTrue(errors.hasErrors());
    }
}