package liquibase.statement.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AlterDatabaseStatement
 */
@DisplayName("AlterDatabaseStatement")
public class AlterDatabaseStatementTest {
    
    private AlterDatabaseStatement statement;
    
    @BeforeEach
    void setUp() {
        statement = new AlterDatabaseStatement();
    }
    
    @Test
    @DisplayName("Should initialize with null values")
    void shouldInitializeWithNullValues() {
        assertNull(statement.getDatabaseName());
        assertNull(statement.getNewName());
        assertNull(statement.getIfExists());
        assertNull(statement.getNewDataRetentionTimeInDays());
        assertNull(statement.getNewMaxDataExtensionTimeInDays());
        assertNull(statement.getNewDefaultDdlCollation());
        assertNull(statement.getNewComment());
        assertNull(statement.getReplaceComment());
        assertNull(statement.getDropComment());
        assertNull(statement.getUnsetDataRetentionTimeInDays());
        assertNull(statement.getUnsetMaxDataExtensionTimeInDays());
        assertNull(statement.getUnsetDefaultDdlCollation());
        assertNull(statement.getUnsetComment());
    }
    
    @Test
    @DisplayName("Should set and get database name")
    void shouldSetAndGetDatabaseName() {
        statement.setDatabaseName("TEST_DB");
        assertEquals("TEST_DB", statement.getDatabaseName());
    }
    
    @Test
    @DisplayName("Should set and get new name")
    void shouldSetAndGetNewName() {
        statement.setNewName("NEW_DB");
        assertEquals("NEW_DB", statement.getNewName());
    }
    
    @Test
    @DisplayName("Should set and get IF EXISTS flag")
    void shouldSetAndGetIfExists() {
        statement.setIfExists(true);
        assertEquals(true, statement.getIfExists());
    }
    
    @Test
    @DisplayName("Should set and get all SET properties")
    void shouldSetAndGetSetProperties() {
        statement.setNewDataRetentionTimeInDays("7");
        statement.setNewMaxDataExtensionTimeInDays("30");
        statement.setNewDefaultDdlCollation("en-ci");
        statement.setNewComment("Test comment");
        statement.setReplaceComment(true);
        statement.setDropComment(true);
        
        assertEquals("7", statement.getNewDataRetentionTimeInDays());
        assertEquals("30", statement.getNewMaxDataExtensionTimeInDays());
        assertEquals("en-ci", statement.getNewDefaultDdlCollation());
        assertEquals("Test comment", statement.getNewComment());
        assertEquals(true, statement.getReplaceComment());
        assertEquals(true, statement.getDropComment());
    }
    
    @Test
    @DisplayName("Should set and get all UNSET properties")
    void shouldSetAndGetUnsetProperties() {
        statement.setUnsetDataRetentionTimeInDays(true);
        statement.setUnsetMaxDataExtensionTimeInDays(true);
        statement.setUnsetDefaultDdlCollation(true);
        statement.setUnsetComment(true);
        
        assertEquals(true, statement.getUnsetDataRetentionTimeInDays());
        assertEquals(true, statement.getUnsetMaxDataExtensionTimeInDays());
        assertEquals(true, statement.getUnsetDefaultDdlCollation());
        assertEquals(true, statement.getUnsetComment());
    }
    
    @Test
    @DisplayName("Should handle mixed operations")
    void shouldHandleMixedOperations() {
        // Mix rename with SET operations
        statement.setDatabaseName("OLD_DB");
        statement.setNewName("NEW_DB");
        statement.setNewDataRetentionTimeInDays("7");
        statement.setUnsetComment(true);
        
        assertEquals("OLD_DB", statement.getDatabaseName());
        assertEquals("NEW_DB", statement.getNewName());
        assertEquals("7", statement.getNewDataRetentionTimeInDays());
        assertEquals(true, statement.getUnsetComment());
    }
}