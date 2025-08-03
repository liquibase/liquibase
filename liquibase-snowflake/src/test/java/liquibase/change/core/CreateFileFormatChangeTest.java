package liquibase.change.core;

import liquibase.change.Change;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateFileFormatStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CreateFileFormatChange
 */
@DisplayName("CreateFileFormatChange")
public class CreateFileFormatChangeTest {
    
    private CreateFileFormatChange change;
    private SnowflakeDatabase database;
    
    @BeforeEach
    void setUp() {
        change = new CreateFileFormatChange();
        database = new SnowflakeDatabase();
    }
    
    @Test
    @DisplayName("Should support Snowflake database")
    void shouldSupportSnowflake() {
        assertTrue(change.supports(database));
    }
    
    @Test
    @DisplayName("Should support rollback for Snowflake database")
    void shouldSupportRollback() {
        assertTrue(change.supportsRollback(database));
    }
    
    @Test
    @DisplayName("Should not support rollback for non-Snowflake database")
    void shouldNotSupportRollbackForNonSnowflake() {
        assertFalse(change.supportsRollback(null));
    }
    
    @Test
    @DisplayName("Should create inverse DropFileFormatChange")
    void shouldCreateInverseDropFileFormat() {
        change.setFileFormatName("TEST_FORMAT");
        change.setCatalogName("TEST_CATALOG");
        change.setSchemaName("TEST_SCHEMA");
        
        Change[] inverses = change.createInverses();
        
        assertNotNull(inverses);
        assertEquals(1, inverses.length);
        assertTrue(inverses[0] instanceof DropFileFormatChange);
        
        DropFileFormatChange dropChange = (DropFileFormatChange) inverses[0];
        assertEquals("TEST_FORMAT", dropChange.getFileFormatName());
        assertEquals("TEST_CATALOG", dropChange.getCatalogName());
        assertEquals("TEST_SCHEMA", dropChange.getSchemaName());
        assertTrue(dropChange.getIfExists());
    }
    
    @Test
    @DisplayName("Should create inverse with minimal properties")
    void shouldCreateInverseWithMinimalProperties() {
        change.setFileFormatName("MINIMAL_FORMAT");
        
        Change[] inverses = change.createInverses();
        
        assertNotNull(inverses);
        assertEquals(1, inverses.length);
        
        DropFileFormatChange dropChange = (DropFileFormatChange) inverses[0];
        assertEquals("MINIMAL_FORMAT", dropChange.getFileFormatName());
        assertNull(dropChange.getCatalogName());
        assertNull(dropChange.getSchemaName());
        assertTrue(dropChange.getIfExists());
    }
    
    @Test
    @DisplayName("Should generate basic file format statement")
    void shouldGenerateBasicFileFormatStatement() {
        change.setFileFormatName("TEST_FORMAT");
        change.setFileFormatType("CSV");
        
        SqlStatement[] statements = change.generateStatements(database);
        
        assertEquals(1, statements.length);
        assertTrue(statements[0] instanceof CreateFileFormatStatement);
        
        CreateFileFormatStatement stmt = (CreateFileFormatStatement) statements[0];
        assertEquals("TEST_FORMAT", stmt.getFileFormatName());
        assertEquals("CSV", stmt.getFileFormatType());
    }
    
    @Test
    @DisplayName("Should require fileFormatName")
    void shouldRequireFileFormatName() {
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().get(0).contains("fileFormatName is required"));
    }
    
    @Test
    @DisplayName("Should validate mutual exclusivity of orReplace and ifNotExists")
    void shouldValidateMutualExclusivity() {
        change.setFileFormatName("TEST_FORMAT");
        change.setOrReplace(true);
        change.setIfNotExists(true);
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Cannot specify both orReplace and ifNotExists")));
    }
    
    @Test
    @DisplayName("Should generate confirmation message")
    void shouldGenerateConfirmationMessage() {
        change.setFileFormatName("TEST_FORMAT");
        
        assertEquals("File format TEST_FORMAT created", change.getConfirmationMessage());
    }
}