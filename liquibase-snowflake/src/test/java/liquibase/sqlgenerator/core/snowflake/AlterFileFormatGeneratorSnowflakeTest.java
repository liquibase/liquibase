package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.core.H2Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AlterFileFormatStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for AlterFileFormatGeneratorSnowflake to achieve 90%+ coverage
 * Focus on addSetOptions, addUnsetOptions, and addFormatTypeChangeUnsetOptions methods
 */
@DisplayName("AlterFileFormatGeneratorSnowflake")
public class AlterFileFormatGeneratorSnowflakeTest {
    
    private AlterFileFormatGeneratorSnowflake generator;
    private SnowflakeDatabase snowflakeDatabase;
    private H2Database h2Database;
    private SqlGeneratorChain mockChain;
    
    @BeforeEach
    void setUp() {
        generator = new AlterFileFormatGeneratorSnowflake();
        snowflakeDatabase = new SnowflakeDatabase();
        h2Database = new H2Database();
        mockChain = null; // Not used in our tests
    }
    
    // ==================== Support Tests ====================
    
    @Test
    @DisplayName("Should support Snowflake database")
    void testSupportsSnowflakeDatabase() {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        statement.setFileFormatName("TEST_FORMAT");
        
        assertTrue(generator.supports(statement, snowflakeDatabase));
    }
    
    @Test
    @DisplayName("Should not support non-Snowflake database")
    void testDoesNotSupportNonSnowflakeDatabase() {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        statement.setFileFormatName("TEST_FORMAT");
        
        assertFalse(generator.supports(statement, h2Database));
    }
    
    // ==================== Validation Tests ====================
    
    @Test
    @DisplayName("Should pass validation with valid statement")
    void testValidationPasses() {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        statement.setFileFormatName("VALID_FORMAT");
        statement.setOperationType("SET");
        
        ValidationErrors errors = generator.validate(statement, snowflakeDatabase, mockChain);
        
        assertFalse(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should fail validation with null file format name")
    void testValidationFailsWithNullFileFormatName() {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        statement.setFileFormatName(null);
        statement.setOperationType("SET");
        
        ValidationErrors errors = generator.validate(statement, snowflakeDatabase, mockChain);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("File format name is required")));
    }
    
    @Test
    @DisplayName("Should fail validation with invalid operation type")
    void testValidationFailsWithInvalidOperationType() {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        statement.setFileFormatName("TEST_FORMAT");
        statement.setOperationType("INVALID");
        
        ValidationErrors errors = generator.validate(statement, snowflakeDatabase, mockChain);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("operationType must be SET, RENAME, or UNSET")));
    }
    
    @Test
    @DisplayName("Should fail validation for RENAME without newFileFormatName")
    void testValidationFailsRenameWithoutNewName() {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        statement.setFileFormatName("TEST_FORMAT");
        statement.setOperationType("RENAME");
        // Missing setNewFileFormatName
        
        ValidationErrors errors = generator.validate(statement, snowflakeDatabase, mockChain);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("newFileFormatName is required for RENAME operation")));
    }
    
    // ==================== SET Operation Tests - addSetOptions Coverage ====================
    
    @Test
    @DisplayName("Should generate SET SQL with basic options")
    void testSetOperationBasicOptions() {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        statement.setFileFormatName("TEST_FORMAT");
        statement.setOperationType("SET");
        statement.setCompression("GZIP");
        statement.setDateFormat("YYYY-MM-DD");
        
        Sql[] sqls = generator.generateSql(statement, snowflakeDatabase, mockChain);
        
        assertNotNull(sqls);
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertEquals("ALTER FILE FORMAT TEST_FORMAT SET COMPRESSION = GZIP, DATE_FORMAT = 'YYYY-MM-DD'", sql);
    }
    
    @Test
    @DisplayName("Should generate SET SQL with format type change")
    void testSetOperationWithFormatTypeChange() {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        statement.setFileFormatName("TEST_FORMAT");
        statement.setOperationType("SET");
        statement.setCurrentFileFormatType("CSV");
        statement.setNewFileFormatType("JSON");
        
        Sql[] sqls = generator.generateSql(statement, snowflakeDatabase, mockChain);
        
        String sql = sqls[0].toSql();
        assertTrue(sql.startsWith("ALTER FILE FORMAT TEST_FORMAT SET TYPE = JSON"));
        // Should contain UNSET options for CSV-specific properties
        assertTrue(sql.contains("RECORD_DELIMITER"));
        assertTrue(sql.contains("FIELD_DELIMITER"));
    }
    
    @Test
    @DisplayName("Should generate SET SQL with CSV-specific options")
    void testSetOperationCsvOptions() {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        statement.setFileFormatName("CSV_FORMAT");
        statement.setOperationType("SET");
        statement.setRecordDelimiter("\\n");
        statement.setFieldDelimiter(",");
        statement.setSkipHeader(1);
        statement.setParseHeader(true);
        statement.setEscape("\\");
        
        Sql[] sqls = generator.generateSql(statement, snowflakeDatabase, mockChain);
        
        assertEquals("ALTER FILE FORMAT CSV_FORMAT SET RECORD_DELIMITER = '\\n', FIELD_DELIMITER = ',', PARSE_HEADER = true, SKIP_HEADER = 1, ESCAPE = '\\'", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate SET SQL with JSON-specific options")
    void testSetOperationJsonOptions() {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        statement.setFileFormatName("JSON_FORMAT");
        statement.setOperationType("SET");
        statement.setEnableOctal(true);
        statement.setAllowDuplicate(false);
        statement.setStripOuterArray(true);
        statement.setStripNullValues(false);
        statement.setIgnoreUtf8Errors(true);
        
        Sql[] sqls = generator.generateSql(statement, snowflakeDatabase, mockChain);
        
        assertEquals("ALTER FILE FORMAT JSON_FORMAT SET ENABLE_OCTAL = true, ALLOW_DUPLICATE = false, STRIP_OUTER_ARRAY = true, STRIP_NULL_VALUES = false, IGNORE_UTF8_ERRORS = true", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate SET SQL with PARQUET-specific options")
    void testSetOperationParquetOptions() {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        statement.setFileFormatName("PARQUET_FORMAT");
        statement.setOperationType("SET");
        statement.setSnappyCompression(true);
        statement.setBinaryAsText(false);
        statement.setUseLogicalType(true);
        statement.setUseVectorizedScanner(false);
        
        Sql[] sqls = generator.generateSql(statement, snowflakeDatabase, mockChain);
        
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("SNAPPY_COMPRESSION = true"));
        assertTrue(sql.contains("BINARY_AS_TEXT = false"));
        assertTrue(sql.contains("USE_LOGICAL_TYPE = true"));
        assertTrue(sql.contains("USE_VECTORIZED_SCANNER = false"));
    }
    
    @Test
    @DisplayName("Should generate SET SQL with XML-specific options")
    void testSetOperationXmlOptions() {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        statement.setFileFormatName("XML_FORMAT");
        statement.setOperationType("SET");
        statement.setPreserveSpace(true);
        statement.setStripOuterElement(false);
        statement.setDisableSnowflakeData(true);
        statement.setDisableAutoConvert(false);
        
        Sql[] sqls = generator.generateSql(statement, snowflakeDatabase, mockChain);
        
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("PRESERVE_SPACE = true"));
        assertTrue(sql.contains("STRIP_OUTER_ELEMENT = false"));
        assertTrue(sql.contains("DISABLE_SNOWFLAKE_DATA = true"));
        assertTrue(sql.contains("DISABLE_AUTO_CONVERT = false"));
    }
    
    @Test
    @DisplayName("Should generate SET SQL with NULL_IF option")
    void testSetOperationNullIfOption() {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        statement.setFileFormatName("TEST_FORMAT");
        statement.setOperationType("SET");
        statement.setNullIf("NULL,EMPTY,N/A");
        
        Sql[] sqls = generator.generateSql(statement, snowflakeDatabase, mockChain);
        
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("NULL_IF = ('NULL', 'EMPTY', 'N/A')"));
    }
    
    @Test
    @DisplayName("Should generate SET SQL with comment")
    void testSetOperationWithComment() {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        statement.setFileFormatName("TEST_FORMAT");
        statement.setOperationType("SET");
        statement.setCompression("GZIP");
        statement.setNewComment("Updated file format");
        
        Sql[] sqls = generator.generateSql(statement, snowflakeDatabase, mockChain);
        
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("COMPRESSION = GZIP"));
        assertTrue(sql.contains("COMMENT = 'Updated file format'"));
    }
    
    @Test
    @DisplayName("Should generate SET SQL with special characters in comment")
    void testSetOperationCommentWithSpecialCharacters() {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        statement.setFileFormatName("TEST_FORMAT");
        statement.setOperationType("SET");
        statement.setNewComment("Comment with 'quotes' and special chars");
        
        Sql[] sqls = generator.generateSql(statement, snowflakeDatabase, mockChain);
        
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("COMMENT = 'Comment with ''quotes'' and special chars'"));
    }
    
    @Test
    @DisplayName("Should generate SET SQL with catalog and schema qualification")
    void testSetOperationWithQualifiedName() {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        statement.setFileFormatName("TEST_FORMAT");
        statement.setCatalogName("TEST_CATALOG");
        statement.setSchemaName("TEST_SCHEMA");
        statement.setOperationType("SET");
        statement.setCompression("GZIP");
        
        Sql[] sqls = generator.generateSql(statement, snowflakeDatabase, mockChain);
        
        String sql = sqls[0].toSql();
        assertTrue(sql.startsWith("ALTER FILE FORMAT TEST_CATALOG.TEST_SCHEMA.TEST_FORMAT SET"));
    }
    
    @Test
    @DisplayName("Should generate SET SQL with IF EXISTS")
    void testSetOperationWithIfExists() {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        statement.setFileFormatName("TEST_FORMAT");
        statement.setOperationType("SET");
        statement.setIfExists(true);
        statement.setCompression("GZIP");
        
        Sql[] sqls = generator.generateSql(statement, snowflakeDatabase, mockChain);
        
        String sql = sqls[0].toSql();
        assertTrue(sql.startsWith("ALTER FILE FORMAT IF EXISTS TEST_FORMAT SET"));
    }
    
    // ==================== Format Type Change Tests ====================
    
    @Test
    @DisplayName("Should generate format change from CSV to JSON with automatic UNSET")
    void testFormatChangeCSVToJSON() {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        statement.setFileFormatName("TEST_FORMAT");
        statement.setOperationType("SET");
        statement.setCurrentFileFormatType("CSV");
        statement.setNewFileFormatType("JSON");
        
        Sql[] sqls = generator.generateSql(statement, snowflakeDatabase, mockChain);
        
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("TYPE = JSON"));
        assertTrue(sql.contains("RECORD_DELIMITER"));
        assertTrue(sql.contains("FIELD_DELIMITER"));
        assertTrue(sql.contains("PARSE_HEADER"));
        assertTrue(sql.contains("SKIP_HEADER"));
        assertTrue(sql.contains("ESCAPE"));
    }
    
    @Test
    @DisplayName("Should generate format change from JSON to PARQUET with automatic UNSET")
    void testFormatChangeJSONToParquet() {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        statement.setFileFormatName("TEST_FORMAT");
        statement.setOperationType("SET");
        statement.setCurrentFileFormatType("JSON");
        statement.setNewFileFormatType("PARQUET");
        
        Sql[] sqls = generator.generateSql(statement, snowflakeDatabase, mockChain);
        
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("TYPE = PARQUET"));
        assertTrue(sql.contains("ENABLE_OCTAL"));
        assertTrue(sql.contains("ALLOW_DUPLICATE"));
        assertTrue(sql.contains("STRIP_OUTER_ARRAY"));
        assertTrue(sql.contains("STRIP_NULL_VALUES"));
        assertTrue(sql.contains("IGNORE_UTF8_ERRORS"));
    }
    
    @Test
    @DisplayName("Should generate format change from PARQUET to XML with automatic UNSET")
    void testFormatChangeParquetToXML() {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        statement.setFileFormatName("TEST_FORMAT");
        statement.setOperationType("SET");
        statement.setCurrentFileFormatType("PARQUET");
        statement.setNewFileFormatType("XML");
        
        Sql[] sqls = generator.generateSql(statement, snowflakeDatabase, mockChain);
        
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("TYPE = XML"));
        assertTrue(sql.contains("SNAPPY_COMPRESSION"));
        assertTrue(sql.contains("BINARY_AS_TEXT"));
        assertTrue(sql.contains("USE_LOGICAL_TYPE"));
        assertTrue(sql.contains("USE_VECTORIZED_SCANNER"));
    }
    
    @Test
    @DisplayName("Should generate format change from XML to CSV with automatic UNSET")
    void testFormatChangeXMLToCSV() {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        statement.setFileFormatName("TEST_FORMAT");
        statement.setOperationType("SET");
        statement.setCurrentFileFormatType("XML");
        statement.setNewFileFormatType("CSV");
        
        Sql[] sqls = generator.generateSql(statement, snowflakeDatabase, mockChain);
        
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("TYPE = CSV"));
        assertTrue(sql.contains("PRESERVE_SPACE"));
        assertTrue(sql.contains("STRIP_OUTER_ELEMENT"));
        assertTrue(sql.contains("DISABLE_SNOWFLAKE_DATA"));
        assertTrue(sql.contains("DISABLE_AUTO_CONVERT"));
    }
    
    @Test
    @DisplayName("Should not add UNSET options when format type doesn't change")
    void testNoFormatChangeNoUnset() {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        statement.setFileFormatName("TEST_FORMAT");
        statement.setOperationType("SET");
        statement.setCurrentFileFormatType("CSV");
        statement.setNewFileFormatType("CSV");
        statement.setCompression("GZIP");
        
        Sql[] sqls = generator.generateSql(statement, snowflakeDatabase, mockChain);
        
        String sql = sqls[0].toSql();
        assertEquals("ALTER FILE FORMAT TEST_FORMAT SET TYPE = CSV, COMPRESSION = GZIP", sql);
        // Should not contain any UNSET operations
        assertFalse(sql.contains("RECORD_DELIMITER"));
    }
    
    @Test
    @DisplayName("Should handle null current format type gracefully")
    void testNullCurrentFormatType() {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        statement.setFileFormatName("TEST_FORMAT");
        statement.setOperationType("SET");
        statement.setCurrentFileFormatType(null);
        statement.setNewFileFormatType("JSON");
        statement.setCompression("GZIP");
        
        Sql[] sqls = generator.generateSql(statement, snowflakeDatabase, mockChain);
        
        String sql = sqls[0].toSql();
        assertEquals("ALTER FILE FORMAT TEST_FORMAT SET TYPE = JSON, COMPRESSION = GZIP", sql);
        // Should not contain any UNSET operations since current type is unknown
        assertFalse(sql.contains("RECORD_DELIMITER"));
    }
    
    // ==================== RENAME Operation Tests ====================
    
    @Test
    @DisplayName("Should generate RENAME SQL")
    void testRenameOperation() {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        statement.setFileFormatName("OLD_FORMAT");
        statement.setOperationType("RENAME");
        statement.setNewFileFormatName("NEW_FORMAT");
        
        Sql[] sqls = generator.generateSql(statement, snowflakeDatabase, mockChain);
        
        String sql = sqls[0].toSql();
        assertEquals("ALTER FILE FORMAT OLD_FORMAT RENAME TO NEW_FORMAT", sql);
    }
    
    @Test
    @DisplayName("Should generate RENAME SQL with IF EXISTS")
    void testRenameOperationWithIfExists() {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        statement.setFileFormatName("OLD_FORMAT");
        statement.setOperationType("RENAME");
        statement.setNewFileFormatName("NEW_FORMAT");
        statement.setIfExists(true);
        
        Sql[] sqls = generator.generateSql(statement, snowflakeDatabase, mockChain);
        
        String sql = sqls[0].toSql();
        assertEquals("ALTER FILE FORMAT IF EXISTS OLD_FORMAT RENAME TO NEW_FORMAT", sql);
    }
    
    // ==================== UNSET Operation Tests - addUnsetOptions Coverage ====================
    
    @Test
    @DisplayName("Should generate UNSET SQL with common options")
    void testUnsetOperationCommonOptions() {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        statement.setFileFormatName("TEST_FORMAT");
        statement.setOperationType("UNSET");
        statement.setUnsetComment(true);
        statement.setUnsetCompression(true);
        statement.setUnsetDateFormat(true);
        
        Sql[] sqls = generator.generateSql(statement, snowflakeDatabase, mockChain);
        
        String sql = sqls[0].toSql();
        assertEquals("ALTER FILE FORMAT TEST_FORMAT UNSET COMMENT, COMPRESSION, DATE_FORMAT", sql);
    }
    
    @Test
    @DisplayName("Should generate UNSET SQL with CSV-specific options")
    void testUnsetOperationCsvOptions() {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        statement.setFileFormatName("CSV_FORMAT");
        statement.setOperationType("UNSET");
        statement.setUnsetRecordDelimiter(true);
        statement.setUnsetFieldDelimiter(true);
        statement.setUnsetSkipHeader(true);
        statement.setUnsetParseHeader(true);
        statement.setUnsetEscape(true);
        
        Sql[] sqls = generator.generateSql(statement, snowflakeDatabase, mockChain);
        
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("RECORD_DELIMITER"));
        assertTrue(sql.contains("FIELD_DELIMITER"));
        assertTrue(sql.contains("SKIP_HEADER"));
        assertTrue(sql.contains("PARSE_HEADER"));
        assertTrue(sql.contains("ESCAPE"));
    }
    
    @Test
    @DisplayName("Should generate UNSET SQL with JSON-specific options")
    void testUnsetOperationJsonOptions() {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        statement.setFileFormatName("JSON_FORMAT");
        statement.setOperationType("UNSET");
        statement.setUnsetEnableOctal(true);
        statement.setUnsetAllowDuplicate(true);
        statement.setUnsetStripOuterArray(true);
        statement.setUnsetStripNullValues(true);
        statement.setUnsetIgnoreUtf8Errors(true);
        
        Sql[] sqls = generator.generateSql(statement, snowflakeDatabase, mockChain);
        
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ENABLE_OCTAL"));
        assertTrue(sql.contains("ALLOW_DUPLICATE"));
        assertTrue(sql.contains("STRIP_OUTER_ARRAY"));
        assertTrue(sql.contains("STRIP_NULL_VALUES"));
        assertTrue(sql.contains("IGNORE_UTF8_ERRORS"));
    }
    
    @Test
    @DisplayName("Should generate UNSET SQL with PARQUET-specific options")
    void testUnsetOperationParquetOptions() {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        statement.setFileFormatName("PARQUET_FORMAT");
        statement.setOperationType("UNSET");
        statement.setUnsetSnappyCompression(true);
        statement.setUnsetBinaryAsText(true);
        statement.setUnsetUseLogicalType(true);
        statement.setUnsetUseVectorizedScanner(true);
        
        Sql[] sqls = generator.generateSql(statement, snowflakeDatabase, mockChain);
        
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("SNAPPY_COMPRESSION"));
        assertTrue(sql.contains("BINARY_AS_TEXT"));
        assertTrue(sql.contains("USE_LOGICAL_TYPE"));
        assertTrue(sql.contains("USE_VECTORIZED_SCANNER"));
    }
    
    @Test
    @DisplayName("Should generate UNSET SQL with XML-specific options")
    void testUnsetOperationXmlOptions() {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        statement.setFileFormatName("XML_FORMAT");
        statement.setOperationType("UNSET");
        statement.setUnsetPreserveSpace(true);
        statement.setUnsetStripOuterElement(true);
        statement.setUnsetDisableSnowflakeData(true);
        statement.setUnsetDisableAutoConvert(true);
        
        Sql[] sqls = generator.generateSql(statement, snowflakeDatabase, mockChain);
        
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("PRESERVE_SPACE"));
        assertTrue(sql.contains("STRIP_OUTER_ELEMENT"));
        assertTrue(sql.contains("DISABLE_SNOWFLAKE_DATA"));
        assertTrue(sql.contains("DISABLE_AUTO_CONVERT"));
    }
    
    @Test
    @DisplayName("Should generate UNSET SQL with mixed format options")
    void testUnsetOperationMixedOptions() {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        statement.setFileFormatName("MIXED_FORMAT");
        statement.setOperationType("UNSET");
        // Common options
        statement.setUnsetCompression(true);
        statement.setUnsetNullIf(true);
        // CSV options
        statement.setUnsetRecordDelimiter(true);
        statement.setUnsetFieldDelimiter(true);
        // JSON options
        statement.setUnsetEnableOctal(true);
        statement.setUnsetAllowDuplicate(true);
        // PARQUET options  
        statement.setUnsetSnappyCompression(true);
        // XML options
        statement.setUnsetPreserveSpace(true);
        
        Sql[] sqls = generator.generateSql(statement, snowflakeDatabase, mockChain);
        
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("COMPRESSION"));
        assertTrue(sql.contains("NULL_IF"));
        assertTrue(sql.contains("RECORD_DELIMITER"));
        assertTrue(sql.contains("FIELD_DELIMITER"));
        assertTrue(sql.contains("ENABLE_OCTAL"));
        assertTrue(sql.contains("ALLOW_DUPLICATE"));
        assertTrue(sql.contains("SNAPPY_COMPRESSION"));
        assertTrue(sql.contains("PRESERVE_SPACE"));
    }
    
    @Test
    @DisplayName("Should generate UNSET SQL with single option")
    void testUnsetOperationSingleOption() {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        statement.setFileFormatName("SINGLE_FORMAT");
        statement.setOperationType("UNSET");
        statement.setUnsetComment(true);
        
        Sql[] sqls = generator.generateSql(statement, snowflakeDatabase, mockChain);
        
        String sql = sqls[0].toSql();
        assertEquals("ALTER FILE FORMAT SINGLE_FORMAT UNSET COMMENT", sql);
    }
    
    @Test
    @DisplayName("Should generate UNSET SQL with qualified name")
    void testUnsetOperationWithQualifiedName() {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        statement.setFileFormatName("TEST_FORMAT");
        statement.setCatalogName("TEST_CATALOG");
        statement.setSchemaName("TEST_SCHEMA");
        statement.setOperationType("UNSET");
        statement.setUnsetCompression(true);
        
        Sql[] sqls = generator.generateSql(statement, snowflakeDatabase, mockChain);
        
        String sql = sqls[0].toSql();
        assertEquals("ALTER FILE FORMAT TEST_CATALOG.TEST_SCHEMA.TEST_FORMAT UNSET COMPRESSION", sql);
    }
    
    @Test
    @DisplayName("Should generate UNSET SQL with IF EXISTS")
    void testUnsetOperationWithIfExists() {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        statement.setFileFormatName("TEST_FORMAT");
        statement.setOperationType("UNSET");
        statement.setIfExists(true);
        statement.setUnsetCompression(true);
        
        Sql[] sqls = generator.generateSql(statement, snowflakeDatabase, mockChain);
        
        String sql = sqls[0].toSql();
        assertEquals("ALTER FILE FORMAT IF EXISTS TEST_FORMAT UNSET COMPRESSION", sql);
    }
    
    @Test
    @DisplayName("Should handle UNSET operation with no options set")
    void testUnsetOperationWithNoOptions() {
        AlterFileFormatStatement statement = new AlterFileFormatStatement();
        statement.setFileFormatName("TEST_FORMAT");
        statement.setOperationType("UNSET");
        // No UNSET flags set to true
        
        Sql[] sqls = generator.generateSql(statement, snowflakeDatabase, mockChain);
        
        String sql = sqls[0].toSql();
        assertEquals("ALTER FILE FORMAT TEST_FORMAT UNSET", sql);
    }
}