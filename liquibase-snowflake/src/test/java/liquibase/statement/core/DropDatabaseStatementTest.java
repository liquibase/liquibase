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
}