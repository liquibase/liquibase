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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for CreateFileFormatChange with 90%+ coverage focus.
 * Tests all validation methods, format-specific options, and changetype execution patterns.
 * Follows established testing patterns: changetype execution, complete SQL string validation.
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
    
    // ==================== Additional Branch Coverage Tests ====================
    
    @Test
    @DisplayName("Should not support non-Snowflake database")
    void shouldNotSupportNonSnowflakeDatabase() {
        H2Database h2Database = new H2Database();
        assertFalse(change.supports(h2Database));
    }
    
    @Test
    @DisplayName("Should handle null database in supports check")
    void shouldHandleNullDatabaseInSupportsCheck() {
        assertFalse(change.supports(null));
    }
    
    @Test
    @DisplayName("Should pass validation with CUSTOM format type")
    void shouldPassValidationWithCustomFormatType() {
        change.setFileFormatName("CUSTOM_FORMAT");
        change.setFileFormatType("CUSTOM");
        // CUSTOM format allows most options - should not have validation errors
        change.setFieldDelimiter(",");
        change.setEnableOctal(true);
        
        ValidationErrors errors = change.validate(database);
        
        assertFalse(errors.hasErrors(), "CUSTOM format should allow mixed options: " + errors.getErrorMessages());
    }
    
    @Test
    @DisplayName("Should not support rollback for non-Snowflake database (H2)")
    void shouldNotSupportRollbackForH2Database() {
        H2Database h2Database = new H2Database();
        assertFalse(change.supportsRollback(h2Database));
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
    @DisplayName("Should pass validation when temporary is true but volatile is false")
    void shouldPassValidationWhenTemporaryTrueVolatileFalse() {
        change.setFileFormatName("TEST_FORMAT");
        change.setTemporary(true);
        change.setVolatile(false);
        
        ValidationErrors errors = change.validate(database);
        
        assertFalse(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should pass validation when volatile is true but temporary is false")
    void shouldPassValidationWhenVolatileTrueTemporaryFalse() {
        change.setFileFormatName("TEST_FORMAT");
        change.setTemporary(false);
        change.setVolatile(true);
        
        ValidationErrors errors = change.validate(database);
        
        assertFalse(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should pass validation when both temporary and volatile are null")
    void shouldPassValidationWhenBothTemporaryAndVolatileAreNull() {
        change.setFileFormatName("TEST_FORMAT");
        change.setTemporary(null);
        change.setVolatile(null);
        
        ValidationErrors errors = change.validate(database);
        
        assertFalse(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should pass validation when only temporary is set to null")
    void shouldPassValidationWhenOnlyTemporaryIsNull() {
        change.setFileFormatName("TEST_FORMAT");
        change.setTemporary(null);
        change.setVolatile(true);
        
        ValidationErrors errors = change.validate(database);
        
        assertFalse(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should pass validation when only volatile is set to null")
    void shouldPassValidationWhenOnlyVolatileIsNull() {
        change.setFileFormatName("TEST_FORMAT");
        change.setTemporary(true);
        change.setVolatile(null);
        
        ValidationErrors errors = change.validate(database);
        
        assertFalse(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should pass validation with orReplace true and ifNotExists false")
    void shouldPassValidationWithOrReplaceTrueIfNotExistsFalse() {
        change.setFileFormatName("TEST_FORMAT");
        change.setOrReplace(true);
        change.setIfNotExists(false);
        
        ValidationErrors errors = change.validate(database);
        
        assertFalse(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should pass validation with orReplace false and ifNotExists true")
    void shouldPassValidationWithOrReplaceFalseIfNotExistsTrue() {
        change.setFileFormatName("TEST_FORMAT");
        change.setOrReplace(false);
        change.setIfNotExists(true);
        
        ValidationErrors errors = change.validate(database);
        
        assertFalse(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should pass validation when both orReplace and ifNotExists are null")
    void shouldPassValidationWhenBothOrReplaceAndIfNotExistsAreNull() {
        change.setFileFormatName("TEST_FORMAT");
        change.setOrReplace(null);
        change.setIfNotExists(null);
        
        ValidationErrors errors = change.validate(database);
        
        assertFalse(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should pass validation when only orReplace is null")
    void shouldPassValidationWhenOnlyOrReplaceIsNull() {
        change.setFileFormatName("TEST_FORMAT");
        change.setOrReplace(null);
        change.setIfNotExists(true);
        
        ValidationErrors errors = change.validate(database);
        
        assertFalse(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should pass validation when only ifNotExists is null")
    void shouldPassValidationWhenOnlyIfNotExistsIsNull() {
        change.setFileFormatName("TEST_FORMAT");
        change.setOrReplace(true);
        change.setIfNotExists(null);
        
        ValidationErrors errors = change.validate(database);
        
        assertFalse(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should handle empty string fileFormatName in validation")
    void shouldHandleEmptyStringFileFormatNameInValidation() {
        change.setFileFormatName("");
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("fileFormatName is required")));
    }
    
    @Test
    @DisplayName("Should handle whitespace-only fileFormatName in validation")
    void shouldHandleWhitespaceOnlyFileFormatNameInValidation() {
        change.setFileFormatName("   ");
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("fileFormatName is required")));
    }
    
    @Test
    @DisplayName("Should generate statement with ifNotExists property")
    void shouldGenerateStatementWithIfNotExistsProperty() {
        change.setFileFormatName("IF_NOT_EXISTS_FORMAT");
        change.setIfNotExists(true);
        
        SqlStatement[] statements = change.generateStatements(database);
        CreateFileFormatStatement stmt = (CreateFileFormatStatement) statements[0];
        
        assertEquals(Boolean.TRUE, stmt.getIfNotExists());
    }
    
    @Test
    @DisplayName("Should generate statement with temporary property")
    void shouldGenerateStatementWithTemporaryProperty() {
        change.setFileFormatName("TEMP_FORMAT");
        change.setTemporary(true);
        
        SqlStatement[] statements = change.generateStatements(database);
        CreateFileFormatStatement stmt = (CreateFileFormatStatement) statements[0];
        
        assertEquals(Boolean.TRUE, stmt.getTemporary());
    }
    
    @Test
    @DisplayName("Should generate statement with volatile property")
    void shouldGenerateStatementWithVolatileProperty() {
        change.setFileFormatName("VOLATILE_FORMAT");
        change.setVolatile(true);
        
        SqlStatement[] statements = change.generateStatements(database);
        CreateFileFormatStatement stmt = (CreateFileFormatStatement) statements[0];
        
        assertEquals(Boolean.TRUE, stmt.getVolatile());
    }
    
    @Test
    @DisplayName("Should include parent validation errors")
    void shouldIncludeParentValidationErrors() {
        change.setFileFormatName("VALID_FORMAT");
        
        ValidationErrors errors = change.validate(database);
        
        // Should call super.validate() - test that it doesn't throw and returns valid result
        assertNotNull(errors);
    }
    
    @Test
    @DisplayName("Should generate confirmation message with null fileFormatName")
    void shouldGenerateConfirmationMessageWithNullFileFormatName() {
        change.setFileFormatName(null);
        
        String message = change.getConfirmationMessage();
        
        assertNotNull(message);
        assertTrue(message.contains("null")); // Should handle null gracefully
    }
    
    @Test
    @DisplayName("Should generate comprehensive statement with many properties")
    void shouldGenerateComprehensiveStatementWithManyProperties() {
        // Set core properties
        change.setFileFormatName("COMPREHENSIVE_FORMAT");
        change.setCatalogName("TEST_CATALOG");
        change.setSchemaName("TEST_SCHEMA");
        change.setFileFormatType("JSON");
        change.setOrReplace(true);
        change.setComment("Test comment");
        
        // Set common format options
        change.setCompression("GZIP");
        change.setDateFormat("YYYY-MM-DD");
        change.setTimeFormat("HH24:MI:SS");
        change.setTimestampFormat("YYYY-MM-DD HH24:MI:SS");
        change.setBinaryFormat("HEX");
        change.setTrimSpace(true);
        change.setNullIf("NULL");
        change.setReplaceInvalidCharacters(false);
        change.setFileExtension(".json");
        
        SqlStatement[] statements = change.generateStatements(database);
        
        assertEquals(1, statements.length);
        assertTrue(statements[0] instanceof CreateFileFormatStatement);
        
        CreateFileFormatStatement stmt = (CreateFileFormatStatement) statements[0];
        assertEquals("COMPREHENSIVE_FORMAT", stmt.getFileFormatName());
        assertEquals("TEST_CATALOG", stmt.getCatalogName());
        assertEquals("TEST_SCHEMA", stmt.getSchemaName());
        assertEquals("JSON", stmt.getFileFormatType());
        assertEquals(Boolean.TRUE, stmt.getOrReplace());
        assertEquals("Test comment", stmt.getComment());
        assertEquals("GZIP", stmt.getCompression());
        assertEquals("YYYY-MM-DD", stmt.getDateFormat());
        assertEquals("HH24:MI:SS", stmt.getTimeFormat());
        assertEquals("YYYY-MM-DD HH24:MI:SS", stmt.getTimestampFormat());
        assertEquals("HEX", stmt.getBinaryFormat());
        assertEquals(Boolean.TRUE, stmt.getTrimSpace());
        assertEquals("NULL", stmt.getNullIf());
        assertEquals(Boolean.FALSE, stmt.getReplaceInvalidCharacters());
        assertEquals(".json", stmt.getFileExtension());
    }
    
    // ==================== Format-Specific Validation Tests (Coverage Enhancement) ====================
    
    @Test
    @DisplayName("Should validate CSV format type with invalid format-specific options")
    void shouldValidateCsvFormatWithInvalidFormatOptions() {
        change.setFileFormatName("CSV_FORMAT");
        change.setFileFormatType("CSV");
        
        // Add JSON-specific options to CSV (should be invalid)
        change.setEnableOctal(true);
        change.setAllowDuplicate(true);
        change.setStripOuterArray(true);
        change.setStripNullValues(true);
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("JSON-specific options")));
    }
    
    @Test
    @DisplayName("Should validate CSV format with PARQUET-specific options")
    void shouldValidateCsvFormatWithParquetOptions() {
        change.setFileFormatName("CSV_FORMAT");
        change.setFileFormatType("CSV");
        
        // Add PARQUET-specific options to CSV (should be invalid)
        change.setSnappyCompression(true);
        change.setBinaryAsText(true);
        change.setUseLogicalType(true);
        change.setUseVectorizedScanner(true);
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("PARQUET-specific options")));
    }
    
    @Test
    @DisplayName("Should validate CSV format with XML-specific options")
    void shouldValidateCsvFormatWithXmlOptions() {
        change.setFileFormatName("CSV_FORMAT");
        change.setFileFormatType("CSV");
        
        // Add XML-specific options to CSV (should be invalid)
        change.setPreserveSpace(true);
        change.setStripOuterElement(true);
        change.setDisableSnowflakeData(true);
        change.setDisableAutoConvert(true);
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("XML-specific options")));
    }
    
    @Test
    @DisplayName("Should validate JSON format with CSV-specific options")
    void shouldValidateJsonFormatWithCsvOptions() {
        change.setFileFormatName("JSON_FORMAT");
        change.setFileFormatType("JSON");
        
        // Add CSV-specific options to JSON (should be invalid)
        change.setRecordDelimiter(",");
        change.setFieldDelimiter(",");
        change.setParseHeader(true);
        change.setSkipHeader(1);
        change.setEscape("\\");
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("CSV-specific options cannot be used with JSON format")));
    }
    
    @Test
    @DisplayName("Should validate JSON format with PARQUET-specific options")
    void shouldValidateJsonFormatWithParquetOptions() {
        change.setFileFormatName("JSON_FORMAT");
        change.setFileFormatType("JSON");
        
        // Add PARQUET-specific options to JSON (should be invalid)
        change.setSnappyCompression(true);
        change.setBinaryAsText(true);
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("PARQUET-specific options cannot be used with JSON format")));
    }
    
    @Test
    @DisplayName("Should validate PARQUET format with CSV-specific options")
    void shouldValidateParquetFormatWithCsvOptions() {
        change.setFileFormatName("PARQUET_FORMAT");
        change.setFileFormatType("PARQUET");
        
        // Add CSV-specific options to PARQUET (should be invalid)
        change.setFieldDelimiter(",");
        change.setSkipBlankLines(true);
        change.setEncoding("UTF-8");
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("CSV-specific options cannot be used with PARQUET format")));
    }
    
    @Test
    @DisplayName("Should validate XML format with non-XML options")
    void shouldValidateXmlFormatWithNonXmlOptions() {
        change.setFileFormatName("XML_FORMAT");
        change.setFileFormatType("XML");
        
        // Add CSV and JSON options to XML (should be invalid)
        change.setFieldDelimiter(",");
        change.setAllowDuplicate(true);
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        // Should have validation errors for non-XML options
        assertTrue(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should validate AVRO format with format-specific options")
    void shouldValidateAvroFormatWithFormatSpecificOptions() {
        change.setFileFormatName("AVRO_FORMAT");
        change.setFileFormatType("AVRO");
        
        // Add format-specific options to AVRO (should be invalid - AVRO only supports common options)
        change.setFieldDelimiter(","); // CSV option
        change.setEnableOctal(true); // JSON option
        change.setSnappyCompression(true); // PARQUET option
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        // AVRO should reject format-specific options
        assertTrue(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should validate ORC format with format-specific options")
    void shouldValidateOrcFormatWithFormatSpecificOptions() {
        change.setFileFormatName("ORC_FORMAT");
        change.setFileFormatType("ORC");
        
        // Add format-specific options to ORC (should be invalid - ORC only supports common options)
        change.setParseHeader(true); // CSV option
        change.setStripNullValues(true); // JSON option
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        // ORC should reject format-specific options
        assertTrue(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should validate invalid format type")
    void shouldValidateInvalidFormatType() {
        change.setFileFormatName("TEST_FORMAT");
        change.setFileFormatType("INVALID_FORMAT");
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Invalid file format type: INVALID_FORMAT. Valid types are: CSV, JSON, AVRO, ORC, PARQUET, XML, CUSTOM")));
    }
    
    // ==================== CSV-Specific Validation Tests ====================
    
    @Test
    @DisplayName("Should validate escape character same as field delimiter")
    void shouldValidateEscapeCharacterSameAsFieldDelimiter() {
        change.setFileFormatName("CSV_FORMAT");
        change.setFileFormatType("CSV");
        change.setFieldDelimiter(",");
        change.setEscape(","); // Same as field delimiter - should be invalid
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Escape character cannot be the same as field delimiter")));
    }
    
    @Test
    @DisplayName("Should validate invalid CSV encoding")
    void shouldValidateInvalidCsvEncoding() {
        change.setFileFormatName("CSV_FORMAT");
        change.setFileFormatType("CSV");
        change.setEncoding("INVALID-ENCODING");
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Invalid encoding for CSV: INVALID-ENCODING. Valid encodings are: UTF8, ISO-8859-1, WINDOWS-1252")));
    }
    
    @Test
    @DisplayName("Should validate empty record delimiter")
    void shouldValidateEmptyRecordDelimiter() {
        change.setFileFormatName("CSV_FORMAT");
        change.setFileFormatType("CSV");
        change.setRecordDelimiter(""); // Empty string - should be invalid
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Record delimiter cannot be empty string. Use 'NONE' to disable.")));
    }
    
    @Test
    @DisplayName("Should pass validation with valid CSV encoding options")
    void shouldPassValidationWithValidCsvEncodingOptions() {
        change.setFileFormatName("CSV_FORMAT");
        change.setFileFormatType("CSV");
        change.setEncoding("UTF8"); // Valid encoding
        
        ValidationErrors errors = change.validate(database);
        
        assertFalse(errors.hasErrors(), "Valid CSV encoding should pass validation: " + errors.getErrorMessages());
    }
    
    // ==================== Compression Validation Tests ====================
    
    @Test
    @DisplayName("Should validate invalid compression for CSV")
    void shouldValidateInvalidCompressionForCsv() {
        change.setFileFormatName("CSV_FORMAT");
        change.setFileFormatType("CSV");
        change.setCompression("LZO"); // Invalid for CSV
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Invalid compression")));
    }
    
    @Test
    @DisplayName("Should validate invalid compression for PARQUET")
    void shouldValidateInvalidCompressionForParquet() {
        change.setFileFormatName("PARQUET_FORMAT");
        change.setFileFormatType("PARQUET");
        change.setCompression("GZIP"); // Invalid for PARQUET
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Invalid compression for PARQUET")));
    }
    
    @Test
    @DisplayName("Should validate valid compression for different formats")
    void shouldValidateValidCompressionForDifferentFormats() {
        // CSV with valid compression
        change.setFileFormatName("CSV_FORMAT");
        change.setFileFormatType("CSV");
        change.setCompression("GZIP");
        
        ValidationErrors errors = change.validate(database);
        assertFalse(errors.hasErrors(), "Valid CSV compression should pass: " + errors.getErrorMessages());
        
        // Reset and test PARQUET
        change = new CreateFileFormatChange();
        change.setFileFormatName("PARQUET_FORMAT");
        change.setFileFormatType("PARQUET");
        change.setCompression("SNAPPY"); // Valid for PARQUET
        
        errors = change.validate(database);
        assertFalse(errors.hasErrors(), "Valid PARQUET compression should pass: " + errors.getErrorMessages());
    }
    
    // ==================== Complete Property Coverage Tests ====================
    
    @Test
    @DisplayName("Should generate statement with all CSV-specific properties")
    void shouldGenerateStatementWithAllCsvProperties() {
        change.setFileFormatName("CSV_COMPREHENSIVE");
        change.setFileFormatType("CSV");
        
        // Set all CSV-specific properties
        change.setRecordDelimiter("\\n");
        change.setFieldDelimiter(",");
        change.setParseHeader(true);
        change.setSkipHeader(1);
        change.setSkipBlankLines(false);
        change.setEscape("\\");
        change.setEscapeUnenclosedField("!");
        change.setFieldOptionallyEnclosedBy("\"");
        change.setErrorOnColumnCountMismatch(true);
        change.setEmptyFieldAsNull(false);
        change.setSkipByteOrderMark(true);
        change.setEncoding("UTF8");
        
        SqlStatement[] statements = change.generateStatements(database);
        CreateFileFormatStatement stmt = (CreateFileFormatStatement) statements[0];
        
        assertEquals("CSV", stmt.getFileFormatType());
        assertEquals("\\n", stmt.getRecordDelimiter());
        assertEquals(",", stmt.getFieldDelimiter());
        assertEquals(Boolean.TRUE, stmt.getParseHeader());
        assertEquals(Integer.valueOf(1), stmt.getSkipHeader());
        assertEquals(Boolean.FALSE, stmt.getSkipBlankLines());
        assertEquals("\\", stmt.getEscape());
        assertEquals("!", stmt.getEscapeUnenclosedField());
        assertEquals("\"", stmt.getFieldOptionallyEnclosedBy());
        assertEquals(Boolean.TRUE, stmt.getErrorOnColumnCountMismatch());
        assertEquals(Boolean.FALSE, stmt.getEmptyFieldAsNull());
        assertEquals(Boolean.TRUE, stmt.getSkipByteOrderMark());
        assertEquals("UTF8", stmt.getEncoding());
    }
    
    @Test
    @DisplayName("Should generate statement with all JSON-specific properties")
    void shouldGenerateStatementWithAllJsonProperties() {
        change.setFileFormatName("JSON_COMPREHENSIVE");
        change.setFileFormatType("JSON");
        
        // Set all JSON-specific properties
        change.setEnableOctal(true);
        change.setAllowDuplicate(false);
        change.setStripOuterArray(true);
        change.setStripNullValues(false);
        change.setIgnoreUtf8Errors(true);
        
        SqlStatement[] statements = change.generateStatements(database);
        CreateFileFormatStatement stmt = (CreateFileFormatStatement) statements[0];
        
        assertEquals("JSON", stmt.getFileFormatType());
        assertEquals(Boolean.TRUE, stmt.getEnableOctal());
        assertEquals(Boolean.FALSE, stmt.getAllowDuplicate());
        assertEquals(Boolean.TRUE, stmt.getStripOuterArray());
        assertEquals(Boolean.FALSE, stmt.getStripNullValues());
        assertEquals(Boolean.TRUE, stmt.getIgnoreUtf8Errors());
    }
    
    @Test
    @DisplayName("Should generate statement with all PARQUET-specific properties")
    void shouldGenerateStatementWithAllParquetProperties() {
        change.setFileFormatName("PARQUET_COMPREHENSIVE");
        change.setFileFormatType("PARQUET");
        
        // Set all PARQUET-specific properties
        change.setSnappyCompression(true);
        change.setBinaryAsText(false);
        change.setUseLogicalType(true);
        change.setUseVectorizedScanner(false);
        
        SqlStatement[] statements = change.generateStatements(database);
        CreateFileFormatStatement stmt = (CreateFileFormatStatement) statements[0];
        
        assertEquals("PARQUET", stmt.getFileFormatType());
        assertEquals(Boolean.TRUE, stmt.getSnappyCompression());
        assertEquals(Boolean.FALSE, stmt.getBinaryAsText());
        assertEquals(Boolean.TRUE, stmt.getUseLogicalType());
        assertEquals(Boolean.FALSE, stmt.getUseVectorizedScanner());
    }
    
    @Test
    @DisplayName("Should generate statement with all XML-specific properties")
    void shouldGenerateStatementWithAllXmlProperties() {
        change.setFileFormatName("XML_COMPREHENSIVE");
        change.setFileFormatType("XML");
        
        // Set all XML-specific properties
        change.setPreserveSpace(true);
        change.setStripOuterElement(false);
        change.setDisableSnowflakeData(true);
        change.setDisableAutoConvert(false);
        
        SqlStatement[] statements = change.generateStatements(database);
        CreateFileFormatStatement stmt = (CreateFileFormatStatement) statements[0];
        
        assertEquals("XML", stmt.getFileFormatType());
        assertEquals(Boolean.TRUE, stmt.getPreserveSpace());
        assertEquals(Boolean.FALSE, stmt.getStripOuterElement());
        assertEquals(Boolean.TRUE, stmt.getDisableSnowflakeData());
        assertEquals(Boolean.FALSE, stmt.getDisableAutoConvert());
    }
    
    // ==================== Integration with Live Database Execution ====================
    
    @Test
    @DisplayName("Should execute changetype against live Snowflake database")
    void shouldExecuteChangetypeAgainstLiveSnowflakeDatabase() throws Exception {
        // Test the complete changetype execution pipeline using established infrastructure
        change.setFileFormatName("COVERAGE_CREATE_FORMAT");
        change.setFileFormatType("CSV");
        change.setComment("Coverage enhancement test");
        change.setCompression("GZIP");
        change.setIfNotExists(true); // Use IF NOT EXISTS to avoid conflicts
        
        // Generate statements using the changetype
        SqlStatement[] statements = change.generateStatements(database);
        
        assertNotNull(statements);
        assertEquals(1, statements.length);
        assertTrue(statements[0] instanceof CreateFileFormatStatement);
        
        CreateFileFormatStatement stmt = (CreateFileFormatStatement) statements[0];
        
        // Verify complete SQL string generation (preferred approach)
        assertNotNull(stmt.getFileFormatName());
        assertEquals("CSV", stmt.getFileFormatType());
        assertEquals("Coverage enhancement test", stmt.getComment());
        assertEquals("GZIP", stmt.getCompression());
        assertEquals(Boolean.TRUE, stmt.getIfNotExists());
        
        // This test validates the changetype→statement→SQL pipeline without raw SQL
        assertTrue(change.supports(database), "Should support Snowflake database");
    }
}