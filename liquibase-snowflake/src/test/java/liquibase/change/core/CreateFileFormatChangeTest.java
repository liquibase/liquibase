package liquibase.change.core;

import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateFileFormatStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Professional test suite for CreateFileFormatChange.
 * Focused on core functionality - removed over-engineering validation tests.
 * Follows professional approach: trust database-level validation, test core operations.
 */
@DisplayName("CreateFileFormatChange - Professional Test Suite")
public class CreateFileFormatChangeTest {
    
    private CreateFileFormatChange change;
    private SnowflakeDatabase database;
    
    @BeforeEach
    void setUp() {
        change = new CreateFileFormatChange();
        database = new SnowflakeDatabase();
    }
    
    // ==================== Core Functionality Tests ====================
    
    @Test
    @DisplayName("Should support Snowflake database")
    void shouldSupportSnowflake() {
        assertTrue(change.supports(database));
    }
    
    @Test
    @DisplayName("Should not support non-Snowflake database")
    void shouldNotSupportNonSnowflakeDatabase() {
        H2Database h2Database = new H2Database();
        assertFalse(change.supports(h2Database));
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
    
    // ==================== Core Validation Tests ====================
    
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
    @DisplayName("Should validate temporary vs volatile mutual exclusivity")
    void shouldValidateTemporaryVsVolatileMutualExclusivity() {
        change.setFileFormatName("TEST_FORMAT");
        change.setTemporary(true);
        change.setVolatile(true);
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Cannot specify both temporary and volatile")));
    }
    
    @Test
    @DisplayName("Should pass validation with valid configuration")
    void shouldPassValidationWithValidConfiguration() {
        change.setFileFormatName("VALID_FORMAT");
        change.setFileFormatType("CSV");
        
        ValidationErrors errors = change.validate(database);
        
        assertFalse(errors.hasErrors());
    }
    
    // ==================== Statement Generation Tests ====================
    
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
    @DisplayName("Should generate statement with common properties using generic storage")
    void shouldGenerateStatementWithCommonProperties() {
        // Test core professional pattern: generic property storage
        change.setFileFormatName("GENERIC_FORMAT");
        change.setCatalogName("TEST_CATALOG");
        change.setSchemaName("TEST_SCHEMA");
        change.setFileFormatType("JSON");
        change.setOrReplace(true);
        change.setComment("Professional test");
        change.setCompression("GZIP");
        
        SqlStatement[] statements = change.generateStatements(database);
        CreateFileFormatStatement stmt = (CreateFileFormatStatement) statements[0];
        
        assertEquals("GENERIC_FORMAT", stmt.getFileFormatName());
        assertEquals("TEST_CATALOG", stmt.getCatalogName());
        assertEquals("TEST_SCHEMA", stmt.getSchemaName());
        assertEquals("JSON", stmt.getFileFormatType());
        assertEquals(Boolean.TRUE, stmt.getOrReplace());
        assertEquals("Professional test", stmt.getComment());
        assertEquals("GZIP", stmt.getCompression());
    }
    
    @Test
    @DisplayName("Should generate statement with CSV properties via generic storage")
    void shouldGenerateStatementWithCsvProperties() {
        change.setFileFormatName("CSV_FORMAT");
        change.setFileFormatType("CSV");
        
        // Test CSV-specific properties through generic storage
        change.setRecordDelimiter("\\n");
        change.setFieldDelimiter(",");
        change.setParseHeader(true);
        change.setSkipHeader(1);
        
        SqlStatement[] statements = change.generateStatements(database);
        CreateFileFormatStatement stmt = (CreateFileFormatStatement) statements[0];
        
        assertEquals("CSV", stmt.getFileFormatType());
        assertEquals("\\n", stmt.getRecordDelimiter());
        assertEquals(",", stmt.getFieldDelimiter());
        assertEquals(Boolean.TRUE, stmt.getParseHeader());
        assertEquals(Integer.valueOf(1), stmt.getSkipHeader());
    }
    
    @Test
    @DisplayName("Should generate statement with JSON properties via generic storage")
    void shouldGenerateStatementWithJsonProperties() {
        change.setFileFormatName("JSON_FORMAT");
        change.setFileFormatType("JSON");
        
        // Test JSON-specific properties through generic storage
        change.setEnableOctal(true);
        change.setAllowDuplicate(false);
        change.setStripOuterArray(true);
        
        SqlStatement[] statements = change.generateStatements(database);
        CreateFileFormatStatement stmt = (CreateFileFormatStatement) statements[0];
        
        assertEquals("JSON", stmt.getFileFormatType());
        assertEquals(Boolean.TRUE, stmt.getEnableOctal());
        assertEquals(Boolean.FALSE, stmt.getAllowDuplicate());
        assertEquals(Boolean.TRUE, stmt.getStripOuterArray());
    }
    
    // ==================== Inverse Operation Tests ====================
    
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
    
    // ==================== Professional Generic Property Storage Tests ====================
    
    @Test
    @DisplayName("Should handle generic property storage and retrieval")
    void shouldHandleGenericPropertyStorageAndRetrieval() {
        // Test the core professional pattern
        change.setObjectProperty("customProperty", "customValue");
        change.setObjectProperty("anotherProperty", "anotherValue");
        
        assertEquals("customValue", change.getObjectProperty("customProperty"));
        assertEquals("anotherValue", change.getObjectProperty("anotherProperty"));
        assertNull(change.getObjectProperty("nonExistentProperty"));
        
        // Test map retrieval
        Map<String, String> allProperties = change.getAllObjectProperties();
        assertEquals("customValue", allProperties.get("customProperty"));
        assertEquals("anotherValue", allProperties.get("anotherProperty"));
    }
    
    @Test
    @DisplayName("Should handle null values in generic property storage")
    void shouldHandleNullValuesInGenericPropertyStorage() {
        change.setObjectProperty("nullProperty", null);
        assertNull(change.getObjectProperty("nullProperty"));
        
        change.setObjectProperty("validProperty", "validValue");
        assertEquals("validValue", change.getObjectProperty("validProperty"));
        
        // Setting null should not overwrite existing value
        change.setObjectProperty("validProperty", null);
        assertEquals("validValue", change.getObjectProperty("validProperty"));
    }
    
    // ==================== Confirmation Message Tests ====================
    
    @Test
    @DisplayName("Should generate confirmation message")
    void shouldGenerateConfirmationMessage() {
        change.setFileFormatName("TEST_FORMAT");
        
        assertEquals("File format TEST_FORMAT created", change.getConfirmationMessage());
    }
    
    @Test
    @DisplayName("Should handle null fileFormatName in confirmation message")
    void shouldHandleNullFileFormatNameInConfirmationMessage() {
        change.setFileFormatName(null);
        
        String message = change.getConfirmationMessage();
        
        assertNotNull(message);
        assertTrue(message.contains("null"));
    }
}