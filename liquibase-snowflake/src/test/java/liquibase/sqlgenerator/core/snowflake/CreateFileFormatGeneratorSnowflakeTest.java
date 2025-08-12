package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.statement.core.CreateFileFormatStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Professional test suite for CreateFileFormatGeneratorSnowflake.
 * Tests SQL generation using generic property storage approach.
 * Updated to match professional refactoring - generic properties with WITH clause.
 */
@DisplayName("CreateFileFormatGeneratorSnowflake - Professional SQL Tests")
public class CreateFileFormatGeneratorSnowflakeTest {
    
    private CreateFileFormatGeneratorSnowflake generator;
    private SnowflakeDatabase database;
    
    @BeforeEach
    void setUp() {
        generator = new CreateFileFormatGeneratorSnowflake();
        database = new SnowflakeDatabase();
    }
    
    // ==================== Core SQL Generation Tests ====================
    
    @Test
    @DisplayName("Should generate basic CREATE FILE FORMAT SQL")
    void testBasicCreateFileFormat() {
        // Given
        CreateFileFormatStatement statement = new CreateFileFormatStatement();
        statement.setFileFormatName("MY_FORMAT");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        assertEquals("CREATE FILE FORMAT MY_FORMAT", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate CREATE OR REPLACE FILE FORMAT SQL")
    void testCreateOrReplaceFileFormat() {
        // Given
        CreateFileFormatStatement statement = new CreateFileFormatStatement();
        statement.setFileFormatName("MY_FORMAT");
        statement.setOrReplace(true);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        assertEquals("CREATE OR REPLACE FILE FORMAT MY_FORMAT", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate CREATE IF NOT EXISTS FILE FORMAT SQL")
    void testCreateIfNotExistsFileFormat() {
        // Given
        CreateFileFormatStatement statement = new CreateFileFormatStatement();
        statement.setFileFormatName("MY_FORMAT");
        statement.setIfNotExists(true);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        assertEquals("CREATE FILE FORMAT IF NOT EXISTS MY_FORMAT", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate TEMPORARY FILE FORMAT SQL")
    void testCreateTemporaryFileFormat() {
        // Given
        CreateFileFormatStatement statement = new CreateFileFormatStatement();
        statement.setFileFormatName("MY_FORMAT");
        statement.setTemporary(true);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        assertEquals("CREATE TEMPORARY FILE FORMAT MY_FORMAT", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate VOLATILE FILE FORMAT SQL")
    void testCreateVolatileFileFormat() {
        // Given
        CreateFileFormatStatement statement = new CreateFileFormatStatement();
        statement.setFileFormatName("MY_FORMAT");
        statement.setVolatile(true);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        assertEquals("CREATE VOLATILE FILE FORMAT MY_FORMAT", sqls[0].toSql());
    }
    
    // ==================== Professional Generic Property Tests ====================
    
    @Test
    @DisplayName("Should generate JSON file format with generic properties (professional approach)")
    void testCreateJsonFileFormatWithGenericProperties() {
        // Given
        CreateFileFormatStatement statement = new CreateFileFormatStatement();
        statement.setFileFormatName("MY_JSON_FORMAT");
        
        // Use generic property storage (professional pattern)
        statement.setObjectProperty("fileFormatType", "JSON");
        statement.setObjectProperty("enableOctal", "false");
        statement.setObjectProperty("stripOuterArray", "true");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String actualSQL = sqls[0].toSql();
        
        // Professional approach: Direct property syntax (correct Snowflake format)
        assertTrue(actualSQL.startsWith("CREATE FILE FORMAT MY_JSON_FORMAT TYPE = JSON"));
        assertTrue(actualSQL.contains("ENABLE_OCTAL = FALSE"));
        assertTrue(actualSQL.contains("STRIP_OUTER_ARRAY = TRUE"));
    }
    
    @Test
    @DisplayName("Should generate CSV file format with compression using generic properties")
    void testCreateCsvFileFormatWithCompressionAndGenericProperties() {
        // Given
        CreateFileFormatStatement statement = new CreateFileFormatStatement();
        statement.setFileFormatName("COMPRESSED_FORMAT");
        statement.setComment("Professional CSV format");
        
        // Use generic property storage (professional pattern)
        statement.setObjectProperty("fileFormatType", "CSV");
        statement.setObjectProperty("compression", "GZIP");
        statement.setObjectProperty("fieldDelimiter", ",");
        statement.setObjectProperty("recordDelimiter", "\\n");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String actualSQL = sqls[0].toSql();
        
        // Professional approach: Direct property syntax (correct Snowflake format)
        assertTrue(actualSQL.startsWith("CREATE FILE FORMAT COMPRESSED_FORMAT TYPE = CSV"));
        assertTrue(actualSQL.contains("COMPRESSION = GZIP"));
        assertTrue(actualSQL.contains("FIELD_DELIMITER = ','"));
        assertTrue(actualSQL.contains("RECORD_DELIMITER = '\\n'"));
        assertTrue(actualSQL.contains("COMMENT = 'Professional CSV format'"));
    }
    
    @Test
    @DisplayName("Should generate file format with schema qualification")
    void testCreateFileFormatWithSchemaQualification() {
        // Given
        CreateFileFormatStatement statement = new CreateFileFormatStatement();
        statement.setFileFormatName("QUALIFIED_FORMAT");
        statement.setCatalogName("TEST_DB");
        statement.setSchemaName("TEST_SCHEMA");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String actualSQL = sqls[0].toSql();
        assertTrue(actualSQL.contains("TEST_DB.TEST_SCHEMA.QUALIFIED_FORMAT"));
    }
    
    // ==================== Professional Property Handling Tests ====================
    
    @Test
    @DisplayName("Should handle boolean properties correctly in generic approach")
    void testBooleanPropertyHandling() {
        // Given
        CreateFileFormatStatement statement = new CreateFileFormatStatement();
        statement.setFileFormatName("BOOLEAN_FORMAT");
        
        // Test boolean property conversion
        statement.setObjectProperty("parseHeader", "true");
        statement.setObjectProperty("skipBlankLines", "false");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String actualSQL = sqls[0].toSql();
        
        assertTrue(actualSQL.contains("PARSE_HEADER = TRUE"));
        assertTrue(actualSQL.contains("SKIP_BLANK_LINES = FALSE"));
    }
    
    @Test
    @DisplayName("Should handle numeric properties correctly in generic approach")
    void testNumericPropertyHandling() {
        // Given
        CreateFileFormatStatement statement = new CreateFileFormatStatement();
        statement.setFileFormatName("NUMERIC_FORMAT");
        
        // Test numeric property (no quotes needed)
        statement.setObjectProperty("skipHeader", "2");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String actualSQL = sqls[0].toSql();
        
        assertTrue(actualSQL.contains("SKIP_HEADER = 2"));
    }
    
    @Test
    @DisplayName("Should handle string properties with proper quoting")
    void testStringPropertyQuoting() {
        // Given
        CreateFileFormatStatement statement = new CreateFileFormatStatement();
        statement.setFileFormatName("STRING_FORMAT");
        
        // Test string properties that need quoting
        statement.setObjectProperty("fieldDelimiter", "|");
        statement.setObjectProperty("dateFormat", "YYYY-MM-DD");
        statement.setObjectProperty("nullIf", "NULL");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String actualSQL = sqls[0].toSql();
        
        assertTrue(actualSQL.contains("FIELD_DELIMITER = '|'"));
        assertTrue(actualSQL.contains("DATE_FORMAT = 'YYYY-MM-DD'"));
        assertTrue(actualSQL.contains("NULL_IF = 'NULL'"));
    }
    
    // ==================== Core Validation Tests ====================
    
    @Test
    @DisplayName("Should require fileFormatName for validation")
    void testValidationRequiresFileFormatName() {
        // Given
        CreateFileFormatStatement statement = new CreateFileFormatStatement();
        // Don't set fileFormatName
        
        // When
        ValidationErrors errors = generator.validate(statement, database, null);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("fileFormatName is required")));
    }
    
    @Test
    @DisplayName("Should pass validation with fileFormatName")
    void testValidationPassesWithFileFormatName() {
        // Given
        CreateFileFormatStatement statement = new CreateFileFormatStatement();
        statement.setFileFormatName("VALID_FORMAT");
        
        // When
        ValidationErrors errors = generator.validate(statement, database, null);
        
        // Then
        assertFalse(errors.hasErrors());
    }
    
    // ==================== Database Support Tests ====================
    
    @Test
    @DisplayName("Should support Snowflake database")
    void testSupportsSnowflakeDatabase() {
        // Given
        CreateFileFormatStatement statement = new CreateFileFormatStatement();
        
        // When/Then
        assertTrue(generator.supports(statement, database));
    }
    
    @Test
    @DisplayName("Should not support non-Snowflake database")
    void testDoesNotSupportNonSnowflakeDatabase() {
        // Given
        CreateFileFormatStatement statement = new CreateFileFormatStatement();
        
        // When/Then
        assertFalse(generator.supports(statement, null));
    }
}