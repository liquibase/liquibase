package liquibase.statement.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CreateSchemaStatement
 */
@DisplayName("CreateSchemaStatement")
public class CreateSchemaStatementTest {
    
    @Test
    @DisplayName("Should initialize with null values")
    public void testInitialState() {
        CreateSchemaStatement statement = new CreateSchemaStatement();
        
        assertNull(statement.getSchemaName());
        assertNull(statement.getComment());
        assertNull(statement.getDataRetentionTimeInDays());
        assertNull(statement.getMaxDataExtensionTimeInDays());
        assertNull(statement.getTransient());
        assertNull(statement.getManaged());
        assertNull(statement.getDefaultDdlCollation());
        assertNull(statement.getPipeExecutionPaused());
        assertNull(statement.getOrReplace());
        assertNull(statement.getIfNotExists());
    }
    
    @Test
    @DisplayName("Should set and get all properties correctly")
    public void testAllProperties() {
        CreateSchemaStatement statement = new CreateSchemaStatement();
        
        // Test all setters and getters
        statement.setSchemaName("TEST_SCHEMA");
        assertEquals("TEST_SCHEMA", statement.getSchemaName());
        
        statement.setComment("Test comment");
        assertEquals("Test comment", statement.getComment());
        
        statement.setDataRetentionTimeInDays("7");
        assertEquals("7", statement.getDataRetentionTimeInDays());
        
        statement.setMaxDataExtensionTimeInDays("30");
        assertEquals("30", statement.getMaxDataExtensionTimeInDays());
        
        statement.setTransient(true);
        assertTrue(statement.getTransient());
        
        statement.setManaged(true);
        assertTrue(statement.getManaged());
        
        statement.setDefaultDdlCollation("en_US");
        assertEquals("en_US", statement.getDefaultDdlCollation());
        
        statement.setPipeExecutionPaused("true");
        assertEquals("true", statement.getPipeExecutionPaused());
        
        statement.setOrReplace(true);
        assertTrue(statement.getOrReplace());
        
        statement.setIfNotExists(true);
        assertTrue(statement.getIfNotExists());
    }
    
    @Test
    @DisplayName("Should handle null values properly")
    public void testNullHandling() {
        CreateSchemaStatement statement = new CreateSchemaStatement();
        
        // Set values then set back to null
        statement.setSchemaName("TEST");
        statement.setSchemaName(null);
        assertNull(statement.getSchemaName());
        
        statement.setTransient(true);
        statement.setTransient(null);
        assertNull(statement.getTransient());
    }
}