package liquibase.statement.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CreateDatabaseStatement
 */
@DisplayName("CreateDatabaseStatement")
public class CreateDatabaseStatementTest {
    
    private CreateDatabaseStatement statement;
    
    @BeforeEach
    void setUp() {
        statement = new CreateDatabaseStatement();
    }
    
    @Test
    @DisplayName("Should have correct initial state")
    void shouldHaveCorrectInitialState() {
        assertNull(statement.getDatabaseName());
        assertNull(statement.getComment());
        assertNull(statement.getDataRetentionTimeInDays());
        assertNull(statement.getMaxDataExtensionTimeInDays());
        assertNull(statement.getTransient());
        assertNull(statement.getDefaultDdlCollation());
        assertNull(statement.getOrReplace());
        assertNull(statement.getIfNotExists());
        assertNull(statement.getCloneFrom());
    }
    
    @Test
    @DisplayName("Should set and get database name")
    void shouldSetAndGetDatabaseName() {
        statement.setDatabaseName("TEST_DB");
        assertEquals("TEST_DB", statement.getDatabaseName());
    }
    
    @Test
    @DisplayName("Should set and get comment")
    void shouldSetAndGetComment() {
        statement.setComment("Test database comment");
        assertEquals("Test database comment", statement.getComment());
    }
    
    @Test
    @DisplayName("Should set and get data retention time")
    void shouldSetAndGetDataRetentionTime() {
        statement.setDataRetentionTimeInDays("7");
        assertEquals("7", statement.getDataRetentionTimeInDays());
    }
    
    @Test
    @DisplayName("Should set and get max data extension time")
    void shouldSetAndGetMaxDataExtensionTime() {
        statement.setMaxDataExtensionTimeInDays("30");
        assertEquals("30", statement.getMaxDataExtensionTimeInDays());
    }
    
    @Test
    @DisplayName("Should set and get transient flag")
    void shouldSetAndGetTransient() {
        statement.setTransient(true);
        assertEquals(true, statement.getTransient());
        
        statement.setTransient(false);
        assertEquals(false, statement.getTransient());
    }
    
    @Test
    @DisplayName("Should set and get default DDL collation")
    void shouldSetAndGetDefaultDdlCollation() {
        statement.setDefaultDdlCollation("en-ci");
        assertEquals("en-ci", statement.getDefaultDdlCollation());
    }
    
    @Test
    @DisplayName("Should set and get OR REPLACE flag")
    void shouldSetAndGetOrReplace() {
        statement.setOrReplace(true);
        assertEquals(true, statement.getOrReplace());
        
        statement.setOrReplace(false);
        assertEquals(false, statement.getOrReplace());
    }
    
    @Test
    @DisplayName("Should set and get IF NOT EXISTS flag")
    void shouldSetAndGetIfNotExists() {
        statement.setIfNotExists(true);
        assertEquals(true, statement.getIfNotExists());
        
        statement.setIfNotExists(false);
        assertEquals(false, statement.getIfNotExists());
    }
    
    @Test
    @DisplayName("Should set and get clone from")
    void shouldSetAndGetCloneFrom() {
        statement.setCloneFrom("SOURCE_DB");
        assertEquals("SOURCE_DB", statement.getCloneFrom());
    }
    
    @Test
    @DisplayName("Should handle null values")
    void shouldHandleNullValues() {
        statement.setDatabaseName(null);
        assertNull(statement.getDatabaseName());
        
        statement.setComment(null);
        assertNull(statement.getComment());
        
        statement.setDataRetentionTimeInDays(null);
        assertNull(statement.getDataRetentionTimeInDays());
        
        statement.setCloneFrom(null);
        assertNull(statement.getCloneFrom());
    }
    
    @Test
    @DisplayName("Should set all properties")
    void shouldSetAllProperties() {
        statement.setDatabaseName("FULL_DB");
        statement.setComment("Full test database");
        statement.setDataRetentionTimeInDays("7");
        statement.setMaxDataExtensionTimeInDays("30");
        statement.setTransient(false);
        statement.setDefaultDdlCollation("en-ci");
        statement.setOrReplace(true);
        statement.setIfNotExists(false);
        statement.setCloneFrom("SOURCE_DB");
        
        assertEquals("FULL_DB", statement.getDatabaseName());
        assertEquals("Full test database", statement.getComment());
        assertEquals("7", statement.getDataRetentionTimeInDays());
        assertEquals("30", statement.getMaxDataExtensionTimeInDays());
        assertEquals(false, statement.getTransient());
        assertEquals("en-ci", statement.getDefaultDdlCollation());
        assertEquals(true, statement.getOrReplace());
        assertEquals(false, statement.getIfNotExists());
        assertEquals("SOURCE_DB", statement.getCloneFrom());
    }
}