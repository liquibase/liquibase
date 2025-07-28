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
}