package liquibase.statement.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DropDatabaseStatement
 */
@DisplayName("DropDatabaseStatement")
public class DropDatabaseStatementTest {
    
    private DropDatabaseStatement statement;
    
    @BeforeEach
    void setUp() {
        statement = new DropDatabaseStatement();
    }
    
    @Test
    @DisplayName("Should have correct initial state")
    void shouldHaveCorrectInitialState() {
        assertNull(statement.getDatabaseName());
        assertNull(statement.getIfExists());
        assertNull(statement.getCascade());
        assertNull(statement.getRestrict());
    }
    
    @Test
    @DisplayName("Should set and get database name")
    void shouldSetAndGetDatabaseName() {
        statement.setDatabaseName("TEST_DB");
        assertEquals("TEST_DB", statement.getDatabaseName());
    }
    
    @Test
    @DisplayName("Should set and get if exists flag")
    void shouldSetAndGetIfExists() {
        statement.setIfExists(true);
        assertEquals(true, statement.getIfExists());
        
        statement.setIfExists(false);
        assertEquals(false, statement.getIfExists());
    }
    
    @Test
    @DisplayName("Should set and get cascade flag")
    void shouldSetAndGetCascade() {
        statement.setCascade(true);
        assertEquals(true, statement.getCascade());
        
        statement.setCascade(false);
        assertEquals(false, statement.getCascade());
    }
    
    @Test
    @DisplayName("Should set and get restrict flag")
    void shouldSetAndGetRestrict() {
        statement.setRestrict(true);
        assertEquals(true, statement.getRestrict());
        
        statement.setRestrict(false);
        assertEquals(false, statement.getRestrict());
    }
    
    @Test
    @DisplayName("Should handle null values")
    void shouldHandleNullValues() {
        statement.setDatabaseName(null);
        assertNull(statement.getDatabaseName());
        
        statement.setIfExists(null);
        assertNull(statement.getIfExists());
        
        statement.setCascade(null);
        assertNull(statement.getCascade());
        
        statement.setRestrict(null);
        assertNull(statement.getRestrict());
    }
    
    @Test
    @DisplayName("Should set all properties")
    void shouldSetAllProperties() {
        statement.setDatabaseName("FULL_DB");
        statement.setIfExists(true);
        statement.setCascade(true);
        statement.setRestrict(false);
        
        assertEquals("FULL_DB", statement.getDatabaseName());
        assertEquals(true, statement.getIfExists());
        assertEquals(true, statement.getCascade());
        assertEquals(false, statement.getRestrict());
    }
    
    // ==================== Validation Tests ====================
    
    @Test
    @DisplayName("Should pass validation with valid database name")
    void shouldPassValidationWithValidDatabaseName() {
        statement.setDatabaseName("VALID_DATABASE");
        
        DropDatabaseStatement.ValidationResult result = statement.validate();
        
        assertFalse(result.hasErrors());
    }
    
    @Test
    @DisplayName("Should fail validation when database name is null")
    void shouldFailValidationWhenDatabaseNameIsNull() {
        statement.setDatabaseName(null);
        
        DropDatabaseStatement.ValidationResult result = statement.validate();
        
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("Database name is required")));
    }
    
    @Test
    @DisplayName("Should fail validation when database name is empty")
    void shouldFailValidationWhenDatabaseNameIsEmpty() {
        statement.setDatabaseName("");
        
        DropDatabaseStatement.ValidationResult result = statement.validate();
        
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("Database name is required")));
    }
    
    @Test
    @DisplayName("Should fail validation when database name is whitespace")
    void shouldFailValidationWhenDatabaseNameIsWhitespace() {
        statement.setDatabaseName("   ");
        
        DropDatabaseStatement.ValidationResult result = statement.validate();
        
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("Database name is required")));
    }
    
    @Test
    @DisplayName("Should fail validation when database name has invalid format")
    void shouldFailValidationWhenDatabaseNameHasInvalidFormat() {
        statement.setDatabaseName("123_INVALID"); // Cannot start with number
        
        DropDatabaseStatement.ValidationResult result = statement.validate();
        
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("Invalid database name format")));
    }
    
    @Test
    @DisplayName("Should fail validation when CASCADE and RESTRICT are both true")
    void shouldFailValidationWhenCascadeAndRestrictBothTrue() {
        statement.setDatabaseName("TEST_DB");
        statement.setCascade(true);
        statement.setRestrict(true);
        
        DropDatabaseStatement.ValidationResult result = statement.validate();
        
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("CASCADE and RESTRICT cannot be used together")));
    }
    
    @Test
    @DisplayName("Should generate warning when IF EXISTS is not used")
    void shouldGenerateWarningWhenIfExistsNotUsed() {
        statement.setDatabaseName("TEST_DB");
        statement.setIfExists(false);
        
        DropDatabaseStatement.ValidationResult result = statement.validate();
        
        assertTrue(result.hasWarnings());
        assertTrue(result.getWarnings().stream()
            .anyMatch(warning -> warning.contains("DROP DATABASE without IF EXISTS will fail if database does not exist")));
    }
    
    @Test
    @DisplayName("Should generate warning when CASCADE is used")
    void shouldGenerateWarningWhenCascadeIsUsed() {
        statement.setDatabaseName("TEST_DB");
        statement.setCascade(true);
        
        DropDatabaseStatement.ValidationResult result = statement.validate();
        
        assertTrue(result.hasWarnings());
        assertTrue(result.getWarnings().stream()
            .anyMatch(warning -> warning.contains("CASCADE will drop all objects within the database permanently")));
    }
    
    @Test
    @DisplayName("Should accept valid database names with underscores and dollar signs")
    void shouldAcceptValidDatabaseNamesWithUnderscoresAndDollarSigns() {
        String[] validNames = {"TEST_DB", "DB_WITH_UNDERSCORE", "DB$WITH$DOLLAR", "MY_TEST$DB_123"};
        
        for (String name : validNames) {
            statement.setDatabaseName(name);
            
            DropDatabaseStatement.ValidationResult result = statement.validate();
            
            assertFalse(result.hasErrors(), "Database name '" + name + "' should be valid");
        }
    }
}