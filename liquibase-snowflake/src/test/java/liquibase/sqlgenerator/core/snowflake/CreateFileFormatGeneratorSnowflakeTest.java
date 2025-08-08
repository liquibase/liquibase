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
 * Pure unit tests for CreateFileFormatGeneratorSnowflake SQL generation
 * Tests SQL string output without database dependencies - NO MOCKING!
 */
@DisplayName("CreateFileFormatGeneratorSnowflake - Pure SQL Tests")
public class CreateFileFormatGeneratorSnowflakeTest {
    
    private CreateFileFormatGeneratorSnowflake generator;
    private SnowflakeDatabase database;
    
    @BeforeEach
    void setUp() {
        generator = new CreateFileFormatGeneratorSnowflake();
        database = new SnowflakeDatabase(); // Real database object, no mocking needed
    }
    
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
    @DisplayName("Should generate CREATE FILE FORMAT IF NOT EXISTS SQL")
    void testCreateFileFormatIfNotExists() {
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
    @DisplayName("Should generate TEMPORARY file format")
    void testCreateTemporaryFileFormat() {
        // Given
        CreateFileFormatStatement statement = new CreateFileFormatStatement();
        statement.setFileFormatName("TEMP_FORMAT");
        statement.setTemporary(true);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        assertEquals("CREATE TEMPORARY FILE FORMAT TEMP_FORMAT", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate CSV file format with options")
    void testCreateCsvFileFormat() {
        // Given
        CreateFileFormatStatement statement = new CreateFileFormatStatement();
        statement.setFileFormatName("MY_CSV_FORMAT");
        statement.setFileFormatType("CSV");
        statement.setFieldDelimiter(",");
        statement.setRecordDelimiter("\\n");
        statement.setSkipHeader(1);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String expectedSQL = "CREATE FILE FORMAT MY_CSV_FORMAT TYPE = CSV RECORD_DELIMITER = '\\n' FIELD_DELIMITER = ',' SKIP_HEADER = 1";
        assertEquals(expectedSQL, sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate JSON file format with options")
    void testCreateJsonFileFormat() {
        // Given
        CreateFileFormatStatement statement = new CreateFileFormatStatement();
        statement.setFileFormatName("MY_JSON_FORMAT");
        statement.setFileFormatType("JSON");
        statement.setStripOuterArray(true);
        statement.setEnableOctal(false);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String expectedSQL = "CREATE FILE FORMAT MY_JSON_FORMAT TYPE = JSON ENABLE_OCTAL = false STRIP_OUTER_ARRAY = true";
        assertEquals(expectedSQL, sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate file format with compression and comment")
    void testCreateFileFormatWithCompressionAndComment() {
        // Given
        CreateFileFormatStatement statement = new CreateFileFormatStatement();
        statement.setFileFormatName("COMPRESSED_FORMAT");
        statement.setFileFormatType("CSV");
        statement.setCompression("GZIP");
        statement.setComment("My compressed CSV format");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String expectedSQL = "CREATE FILE FORMAT COMPRESSED_FORMAT TYPE = CSV COMPRESSION = GZIP COMMENT = 'My compressed CSV format'";
        assertEquals(expectedSQL, sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate file format with schema qualification")
    void testCreateFileFormatWithSchema() {
        // Given
        CreateFileFormatStatement statement = new CreateFileFormatStatement();
        statement.setFileFormatName("MY_FORMAT");
        statement.setSchemaName("MY_SCHEMA");
        statement.setCatalogName("MY_DB");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        assertEquals("CREATE FILE FORMAT MY_DB.MY_SCHEMA.MY_FORMAT", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should validate file format name is required")
    void testValidationRequiresFileFormatName() {
        // Given
        CreateFileFormatStatement statement = new CreateFileFormatStatement();
        // No file format name set
        
        // When
        ValidationErrors errors = generator.validate(statement, database, null);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().get(0).contains("File format name is required"));
    }
    
    @Test
    @DisplayName("Should validate OR REPLACE vs IF NOT EXISTS conflict")
    void testValidationOrReplaceVsIfNotExists() {
        // Given
        CreateFileFormatStatement statement = new CreateFileFormatStatement();
        statement.setFileFormatName("MY_FORMAT");
        statement.setOrReplace(true);
        statement.setIfNotExists(true);
        
        // When
        ValidationErrors errors = generator.validate(statement, database, null);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().get(0).contains("OR REPLACE and IF NOT EXISTS cannot be used together"));
    }
    
    @Test
    @DisplayName("Should validate TEMPORARY vs VOLATILE conflict")
    void testValidationTemporaryVsVolatile() {
        // Given
        CreateFileFormatStatement statement = new CreateFileFormatStatement();
        statement.setFileFormatName("MY_FORMAT");
        statement.setTemporary(true);
        statement.setVolatile(true);
        
        // When
        ValidationErrors errors = generator.validate(statement, database, null);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().get(0).contains("TEMPORARY and VOLATILE cannot be used together"));
    }
    
    @Test
    @DisplayName("Should validate invalid file format type")
    void testValidationInvalidFileFormatType() {
        // Given
        CreateFileFormatStatement statement = new CreateFileFormatStatement();
        statement.setFileFormatName("MY_FORMAT");
        statement.setFileFormatType("INVALID_TYPE");
        
        // When
        ValidationErrors errors = generator.validate(statement, database, null);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().get(0).contains("Invalid file format type 'INVALID_TYPE'"));
    }
    
    @Test
    @DisplayName("Should validate character delimiter conflicts")
    void testValidationCharacterDelimiterConflicts() {
        // Given
        CreateFileFormatStatement statement = new CreateFileFormatStatement();
        statement.setFileFormatName("MY_FORMAT");
        statement.setFieldDelimiter(",");
        statement.setEscape(",");
        
        // When
        ValidationErrors errors = generator.validate(statement, database, null);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().get(0).contains("ESCAPE character cannot be the same as FIELD_DELIMITER"));
    }
    
    @Test
    @DisplayName("Should validate field delimiter single character constraint")
    void testValidationSingleCharacterDelimiter() {
        // Given
        CreateFileFormatStatement statement = new CreateFileFormatStatement();
        statement.setFileFormatName("MY_FORMAT");
        statement.setFieldDelimiter("ab"); // Invalid: multiple characters
        
        // When
        ValidationErrors errors = generator.validate(statement, database, null);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().get(0).contains("FIELD_DELIMITER must be a single character"));
    }
    
    @Test
    @DisplayName("Should support Snowflake database")
    void testSupportsSnowflakeDatabase() {
        // Given
        CreateFileFormatStatement statement = new CreateFileFormatStatement();
        
        // When/Then
        assertTrue(generator.supports(statement, database));
    }
}