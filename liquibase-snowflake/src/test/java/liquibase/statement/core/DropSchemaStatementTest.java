package liquibase.statement.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DropSchemaStatement
 */
@DisplayName("DropSchemaStatement")
public class DropSchemaStatementTest {
    
    @Test
    @DisplayName("Should initialize with null values")
    public void testInitialState() {
        DropSchemaStatement statement = new DropSchemaStatement();
        
        assertNull(statement.getSchemaName());
        assertNull(statement.getCascade());
        assertNull(statement.getIfExists());
        assertNull(statement.getRestrict());
    }
    
    @Test
    @DisplayName("Should set and get all properties correctly")
    public void testAllProperties() {
        DropSchemaStatement statement = new DropSchemaStatement();
        
        // Test all setters and getters
        statement.setSchemaName("TEST_SCHEMA");
        assertEquals("TEST_SCHEMA", statement.getSchemaName());
        
        statement.setCascade(true);
        assertTrue(statement.getCascade());
        
        statement.setIfExists(true);
        assertTrue(statement.getIfExists());
        
        statement.setRestrict(true);
        assertTrue(statement.getRestrict());
    }
    
    @Test
    @DisplayName("Should handle Boolean properties correctly")
    public void testBooleanProperties() {
        DropSchemaStatement statement = new DropSchemaStatement();
        
        // Test setting to false
        statement.setCascade(false);
        assertFalse(statement.getCascade());
        
        statement.setIfExists(false);
        assertFalse(statement.getIfExists());
        
        statement.setRestrict(false);
        assertFalse(statement.getRestrict());
    }
    
    @Test
    @DisplayName("Should handle null values properly")
    public void testNullHandling() {
        DropSchemaStatement statement = new DropSchemaStatement();
        
        // Set values then set back to null
        statement.setSchemaName("TEST");
        statement.setSchemaName(null);
        assertNull(statement.getSchemaName());
        
        statement.setCascade(true);
        statement.setCascade(null);
        assertNull(statement.getCascade());
        
        statement.setIfExists(true);
        statement.setIfExists(null);
        assertNull(statement.getIfExists());
        
        statement.setRestrict(true);
        statement.setRestrict(null);
        assertNull(statement.getRestrict());
    }
    
    // ==================== Validation Tests ====================
    
    @Test
    @DisplayName("Should pass validation with valid schema name")
    void shouldPassValidationWithValidSchemaName() {
        DropSchemaStatement statement = new DropSchemaStatement();
        statement.setSchemaName("VALID_SCHEMA");
        
        DropSchemaStatement.ValidationResult result = statement.validate();
        
        assertFalse(result.hasErrors());
    }
    
    @Test
    @DisplayName("Should fail validation when schema name is null")
    void shouldFailValidationWhenSchemaNameIsNull() {
        DropSchemaStatement statement = new DropSchemaStatement();
        statement.setSchemaName(null);
        
        DropSchemaStatement.ValidationResult result = statement.validate();
        
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("Schema name is required")));
    }
    
    @Test
    @DisplayName("Should fail validation when schema name is empty")
    void shouldFailValidationWhenSchemaNameIsEmpty() {
        DropSchemaStatement statement = new DropSchemaStatement();
        statement.setSchemaName("");
        
        DropSchemaStatement.ValidationResult result = statement.validate();
        
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("Schema name is required")));
    }
    
    @Test
    @DisplayName("Should fail validation when CASCADE and RESTRICT are both true")
    void shouldFailValidationWhenCascadeAndRestrictBothTrue() {
        DropSchemaStatement statement = new DropSchemaStatement();
        statement.setSchemaName("TEST_SCHEMA");
        statement.setCascade(true);
        statement.setRestrict(true);
        
        DropSchemaStatement.ValidationResult result = statement.validate();
        
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("CASCADE and RESTRICT cannot be used together")));
    }
    
    @Test
    @DisplayName("Should generate warning when IF EXISTS is not used")
    void shouldGenerateWarningWhenIfExistsNotUsed() {
        DropSchemaStatement statement = new DropSchemaStatement();
        statement.setSchemaName("TEST_SCHEMA");
        statement.setIfExists(false);
        
        DropSchemaStatement.ValidationResult result = statement.validate();
        
        assertTrue(result.hasWarnings());
        assertTrue(result.getWarnings().stream()
            .anyMatch(warning -> warning.contains("DROP SCHEMA without IF EXISTS will fail if schema does not exist")));
    }
    
    @Test
    @DisplayName("Should generate warning when CASCADE is used")
    void shouldGenerateWarningWhenCascadeIsUsed() {
        DropSchemaStatement statement = new DropSchemaStatement();
        statement.setSchemaName("TEST_SCHEMA");
        statement.setCascade(true);
        
        DropSchemaStatement.ValidationResult result = statement.validate();
        
        assertTrue(result.hasWarnings());
        assertTrue(result.getWarnings().stream()
            .anyMatch(warning -> warning.contains("CASCADE will drop all objects within the schema permanently")));
    }
    
    @Test
    @DisplayName("Should accept valid schema names with underscores and dollar signs")
    void shouldAcceptValidSchemaNamesWithUnderscoresAndDollarSigns() {
        DropSchemaStatement statement = new DropSchemaStatement();
        String[] validNames = {"TEST_SCHEMA", "SCHEMA_WITH_UNDERSCORE", "SCHEMA$WITH$DOLLAR", "MY_TEST$SCHEMA_123"};
        
        for (String name : validNames) {
            statement.setSchemaName(name);
            
            DropSchemaStatement.ValidationResult result = statement.validate();
            
            assertFalse(result.hasErrors(), "Schema name '" + name + "' should be valid");
        }
    }
}