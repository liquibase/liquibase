package liquibase.change.core;

import liquibase.change.Change;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AlterFileFormatChange rollback functionality
 */
@DisplayName("AlterFileFormatChange")
public class AlterFileFormatChangeTest {
    
    private AlterFileFormatChange change;
    private SnowflakeDatabase database;
    
    @BeforeEach
    void setUp() {
        change = new AlterFileFormatChange();
        database = new SnowflakeDatabase();
    }
    
    @Test
    @DisplayName("Should support rollback for RENAME operations")
    void shouldSupportRollbackForRename() {
        change.setOperationType("RENAME");
        
        assertTrue(change.supportsRollback(database));
    }
    
    @Test
    @DisplayName("Should not support rollback for SET operations")
    void shouldNotSupportRollbackForSet() {
        change.setOperationType("SET");
        
        assertFalse(change.supportsRollback(database));
    }
    
    @Test
    @DisplayName("Should not support rollback for UNSET operations")
    void shouldNotSupportRollbackForUnset() {
        change.setOperationType("UNSET");
        
        assertFalse(change.supportsRollback(database));
    }
    
    @Test
    @DisplayName("Should not support rollback when operation type is null")
    void shouldNotSupportRollbackWhenOperationTypeNull() {
        change.setOperationType(null);
        
        assertFalse(change.supportsRollback(database));
    }
    
    @Test
    @DisplayName("Should create inverse RENAME operation")
    void shouldCreateInverseRenameOperation() {
        change.setFileFormatName("OLD_FORMAT");
        change.setNewFileFormatName("NEW_FORMAT");
        change.setOperationType("RENAME");
        change.setCatalogName("TEST_CATALOG");
        change.setSchemaName("TEST_SCHEMA");
        
        Change[] inverses = change.createInverses();
        
        assertNotNull(inverses);
        assertEquals(1, inverses.length);
        assertTrue(inverses[0] instanceof AlterFileFormatChange);
        
        AlterFileFormatChange inverseChange = (AlterFileFormatChange) inverses[0];
        assertEquals("NEW_FORMAT", inverseChange.getFileFormatName());
        assertEquals("OLD_FORMAT", inverseChange.getNewFileFormatName());
        assertEquals("RENAME", inverseChange.getOperationType());
        assertEquals("TEST_CATALOG", inverseChange.getCatalogName());
        assertEquals("TEST_SCHEMA", inverseChange.getSchemaName());
        assertTrue(inverseChange.getIfExists());
    }
    
    @Test
    @DisplayName("Should return empty inverses for SET operations")
    void shouldReturnEmptyInversesForSet() {
        change.setFileFormatName("TEST_FORMAT");
        change.setOperationType("SET");
        
        Change[] inverses = change.createInverses();
        
        assertNotNull(inverses);
        assertEquals(0, inverses.length);
    }
    
    @Test
    @DisplayName("Should return empty inverses when newFileFormatName is null")
    void shouldReturnEmptyInversesWhenNewNameNull() {
        change.setFileFormatName("TEST_FORMAT");
        change.setOperationType("RENAME");
        change.setNewFileFormatName(null);
        
        Change[] inverses = change.createInverses();
        
        assertNotNull(inverses);
        assertEquals(0, inverses.length);
    }
    
    @Test
    @DisplayName("Should require fileFormatName")
    void shouldRequireFileFormatName() {
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().get(0).contains("fileFormatName is required"));
    }
    
    @Test
    @DisplayName("Should require newFileFormatName for RENAME operation")
    void shouldRequireNewFileFormatNameForRename() {
        change.setFileFormatName("TEST_FORMAT");
        change.setOperationType("RENAME");
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("newFileFormatName is required for RENAME operation")));
    }
}