package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.CreateFileFormatChange;
import liquibase.database.core.H2Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.object.FileFormat;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test for MissingFileFormatChangeGenerator.
 * Covers priority logic, property mapping, and all FileFormat attributes.
 */
@DisplayName("MissingFileFormatChangeGenerator - Comprehensive Test")
public class MissingFileFormatChangeGeneratorTest {

    private MissingFileFormatChangeGenerator generator;
    private SnowflakeDatabase database;
    private FileFormat fileFormat;
    private DiffOutputControl control;
    private ChangeGeneratorChain chain;

    @BeforeEach
    void setUp() {
        generator = new MissingFileFormatChangeGenerator();
        database = new SnowflakeDatabase();
        
        Catalog catalog = new Catalog("TEST_CATALOG");
        Schema schema = new Schema(catalog, "TEST_SCHEMA");
        fileFormat = new FileFormat();
        fileFormat.setName("TEST_FILE_FORMAT");
        fileFormat.setSchema(schema);
        
        control = mock(DiffOutputControl.class);
        chain = mock(ChangeGeneratorChain.class);
    }

    // ==================== Priority Tests ====================

    @Test
    @DisplayName("Should have high priority for Snowflake FileFormat objects")
    void shouldHaveHighPriorityForSnowflakeFileFormatObjects() {
        int priority = generator.getPriority(FileFormat.class, database);
        assertEquals(MissingFileFormatChangeGenerator.PRIORITY_DATABASE, priority);
    }

    @Test
    @DisplayName("Should have no priority for non-Snowflake databases")
    void shouldHaveNoPriorityForNonSnowflakeDatabases() {
        H2Database h2Database = new H2Database();
        int priority = generator.getPriority(FileFormat.class, h2Database);
        assertEquals(MissingFileFormatChangeGenerator.PRIORITY_NONE, priority);
    }

    @Test
    @DisplayName("Should have no priority for non-FileFormat objects")
    void shouldHaveNoPriorityForNonFileFormatObjects() {
        int priority = generator.getPriority(Schema.class, database);
        assertEquals(MissingFileFormatChangeGenerator.PRIORITY_NONE, priority);
    }

    // ==================== Basic Functionality Tests ====================

    @Test
    @DisplayName("Should generate CreateFileFormatChange with file format name")
    void shouldGenerateCreateFileFormatChangeWithFileName() {
        Change[] changes = generator.fixMissing(fileFormat, control, database, database, chain);
        
        assertNotNull(changes);
        assertEquals(1, changes.length);
        assertTrue(changes[0] instanceof CreateFileFormatChange);
        
        CreateFileFormatChange createChange = (CreateFileFormatChange) changes[0];
        assertEquals("TEST_FILE_FORMAT", createChange.getFileFormatName());
    }

    @Test
    @DisplayName("Should map format type property")
    void shouldMapFormatTypeProperty() {
        fileFormat.setFormatType("CSV");
        
        Change[] changes = generator.fixMissing(fileFormat, control, database, database, chain);
        
        CreateFileFormatChange createChange = (CreateFileFormatChange) changes[0];
        assertEquals("CSV", createChange.getFileFormatType());
    }

    @Test
    @DisplayName("Should handle null format type")
    void shouldHandleNullFormatType() {
        fileFormat.setFormatType(null);
        
        Change[] changes = generator.fixMissing(fileFormat, control, database, database, chain);
        
        CreateFileFormatChange createChange = (CreateFileFormatChange) changes[0];
        assertNull(createChange.getFileFormatType());
    }

    // ==================== CSV Format Properties Tests ====================

    @Test
    @DisplayName("Should map CSV record delimiter property")
    void shouldMapCsvRecordDelimiterProperty() {
        fileFormat.setRecordDelimiter("\\n");
        
        Change[] changes = generator.fixMissing(fileFormat, control, database, database, chain);
        
        CreateFileFormatChange createChange = (CreateFileFormatChange) changes[0];
        assertEquals("\\n", createChange.getRecordDelimiter());
    }

    @Test
    @DisplayName("Should map CSV field delimiter property")
    void shouldMapCsvFieldDelimiterProperty() {
        fileFormat.setFieldDelimiter(",");
        
        Change[] changes = generator.fixMissing(fileFormat, control, database, database, chain);
        
        CreateFileFormatChange createChange = (CreateFileFormatChange) changes[0];
        assertEquals(",", createChange.getFieldDelimiter());
    }

    @Test
    @DisplayName("Should map CSV skip header property")
    void shouldMapCsvSkipHeaderProperty() {
        fileFormat.setSkipHeader(5);
        
        Change[] changes = generator.fixMissing(fileFormat, control, database, database, chain);
        
        CreateFileFormatChange createChange = (CreateFileFormatChange) changes[0];
        assertEquals(Integer.valueOf(5), createChange.getSkipHeader());
    }

    @Test
    @DisplayName("Should map CSV field optionally enclosed by property")
    void shouldMapCsvFieldOptionallyEnclosedByProperty() {
        fileFormat.setFieldOptionallyEnclosedBy("\"");
        
        Change[] changes = generator.fixMissing(fileFormat, control, database, database, chain);
        
        CreateFileFormatChange createChange = (CreateFileFormatChange) changes[0];
        assertEquals("\"", createChange.getFieldOptionallyEnclosedBy());
    }

    @Test
    @DisplayName("Should map CSV escape property")
    void shouldMapCsvEscapeProperty() {
        fileFormat.setEscape("\\\\");
        
        Change[] changes = generator.fixMissing(fileFormat, control, database, database, chain);
        
        CreateFileFormatChange createChange = (CreateFileFormatChange) changes[0];
        assertEquals("\\\\", createChange.getEscape());
    }

    @Test
    @DisplayName("Should map CSV trim space property")
    void shouldMapCsvTrimSpaceProperty() {
        fileFormat.setTrimSpace(true);
        
        Change[] changes = generator.fixMissing(fileFormat, control, database, database, chain);
        
        CreateFileFormatChange createChange = (CreateFileFormatChange) changes[0];
        assertEquals(Boolean.TRUE, createChange.getTrimSpace());
    }

    @Test
    @DisplayName("Should map CSV escape unenclosed field property")
    void shouldMapCsvEscapeUnenclosedFieldProperty() {
        fileFormat.setEscapeUnenclosedField("\\\\");
        
        Change[] changes = generator.fixMissing(fileFormat, control, database, database, chain);
        
        CreateFileFormatChange createChange = (CreateFileFormatChange) changes[0];
        assertEquals("\\\\", createChange.getEscapeUnenclosedField());
    }

    @Test
    @DisplayName("Should map CSV error on column count mismatch property")
    void shouldMapCsvErrorOnColumnCountMismatchProperty() {
        fileFormat.setErrorOnColumnCountMismatch(true);
        
        Change[] changes = generator.fixMissing(fileFormat, control, database, database, chain);
        
        CreateFileFormatChange createChange = (CreateFileFormatChange) changes[0];
        assertEquals(Boolean.TRUE, createChange.getErrorOnColumnCountMismatch());
    }

    // ==================== Compression Tests ====================

    @Test
    @DisplayName("Should map compression property")
    void shouldMapCompressionProperty() {
        fileFormat.setCompression("GZIP");
        
        Change[] changes = generator.fixMissing(fileFormat, control, database, database, chain);
        
        CreateFileFormatChange createChange = (CreateFileFormatChange) changes[0];
        assertEquals("GZIP", createChange.getCompression());
    }

    @Test
    @DisplayName("Should handle various compression types")
    void shouldHandleVariousCompressionTypes() {
        String[] compressionTypes = {"GZIP", "BZ2", "BROTLI", "ZSTD", "DEFLATE", "RAW_DEFLATE", "NONE"};
        
        for (String compressionType : compressionTypes) {
            fileFormat.setCompression(compressionType);
            
            Change[] changes = generator.fixMissing(fileFormat, control, database, database, chain);
            
            CreateFileFormatChange createChange = (CreateFileFormatChange) changes[0];
            assertEquals(compressionType, createChange.getCompression(),
                         "Compression type should be set correctly: " + compressionType);
        }
    }

    // ==================== Date/Time Format Tests ====================

    @Test
    @DisplayName("Should map date format property")
    void shouldMapDateFormatProperty() {
        fileFormat.setDateFormat("YYYY-MM-DD");
        
        Change[] changes = generator.fixMissing(fileFormat, control, database, database, chain);
        
        CreateFileFormatChange createChange = (CreateFileFormatChange) changes[0];
        assertEquals("YYYY-MM-DD", createChange.getDateFormat());
    }

    @Test
    @DisplayName("Should map time format property")
    void shouldMapTimeFormatProperty() {
        fileFormat.setTimeFormat("HH24:MI:SS");
        
        Change[] changes = generator.fixMissing(fileFormat, control, database, database, chain);
        
        CreateFileFormatChange createChange = (CreateFileFormatChange) changes[0];
        assertEquals("HH24:MI:SS", createChange.getTimeFormat());
    }

    @Test
    @DisplayName("Should map timestamp format property")
    void shouldMapTimestampFormatProperty() {
        fileFormat.setTimestampFormat("YYYY-MM-DD HH24:MI:SS");
        
        Change[] changes = generator.fixMissing(fileFormat, control, database, database, chain);
        
        CreateFileFormatChange createChange = (CreateFileFormatChange) changes[0];
        assertEquals("YYYY-MM-DD HH24:MI:SS", createChange.getTimestampFormat());
    }

    // ==================== Binary Format Tests ====================

    @Test
    @DisplayName("Should map binary format property")
    void shouldMapBinaryFormatProperty() {
        fileFormat.setBinaryFormat("HEX");
        
        Change[] changes = generator.fixMissing(fileFormat, control, database, database, chain);
        
        CreateFileFormatChange createChange = (CreateFileFormatChange) changes[0];
        assertEquals("HEX", createChange.getBinaryFormat());
    }

    @Test
    @DisplayName("Should handle different binary format values")
    void shouldHandleDifferentBinaryFormatValues() {
        String[] binaryFormats = {"HEX", "BASE64", "UTF8"};
        
        for (String binaryFormat : binaryFormats) {
            fileFormat.setBinaryFormat(binaryFormat);
            
            Change[] changes = generator.fixMissing(fileFormat, control, database, database, chain);
            
            CreateFileFormatChange createChange = (CreateFileFormatChange) changes[0];
            assertEquals(binaryFormat, createChange.getBinaryFormat(),
                         "Binary format should be set correctly: " + binaryFormat);
        }
    }

    // ==================== Null Handling Tests ====================

    @Test
    @DisplayName("Should map null if property")
    void shouldMapNullIfProperty() {
        fileFormat.setNullIf("\\\\N");
        
        Change[] changes = generator.fixMissing(fileFormat, control, database, database, chain);
        
        CreateFileFormatChange createChange = (CreateFileFormatChange) changes[0];
        assertEquals("\\\\N", createChange.getNullIf());
    }

    @Test
    @DisplayName("Should handle various null if values")
    void shouldHandleVariousNullIfValues() {
        String[] nullValues = {"\\\\N", "NULL", "", "\\\\NULL", "EMPTY"};
        
        for (String nullValue : nullValues) {
            fileFormat.setNullIf(nullValue);
            
            Change[] changes = generator.fixMissing(fileFormat, control, database, database, chain);
            
            CreateFileFormatChange createChange = (CreateFileFormatChange) changes[0];
            assertEquals(nullValue, createChange.getNullIf(),
                         "Null value should be set correctly: " + nullValue);
        }
    }

    // ==================== Complete FileFormat Configuration Tests ====================

    @Test
    @DisplayName("Should map complete CSV file format configuration")
    void shouldMapCompleteCsvFileFormatConfiguration() {
        // Setup complete CSV configuration
        fileFormat.setFormatType("CSV");
        fileFormat.setRecordDelimiter("\\n");
        fileFormat.setFieldDelimiter(",");
        fileFormat.setSkipHeader(1);
        fileFormat.setFieldOptionallyEnclosedBy("\"");
        fileFormat.setEscape("\\\\");
        fileFormat.setTrimSpace(true);
        fileFormat.setCompression("GZIP");
        fileFormat.setDateFormat("YYYY-MM-DD");
        fileFormat.setTimeFormat("HH24:MI:SS");
        fileFormat.setTimestampFormat("YYYY-MM-DD HH24:MI:SS");
        fileFormat.setBinaryFormat("HEX");
        fileFormat.setNullIf("\\\\N");
        fileFormat.setEscapeUnenclosedField("\\\\");
        fileFormat.setErrorOnColumnCountMismatch(false);
        
        Change[] changes = generator.fixMissing(fileFormat, control, database, database, chain);
        
        CreateFileFormatChange createChange = (CreateFileFormatChange) changes[0];
        
        // Verify all properties are mapped
        assertEquals("TEST_FILE_FORMAT", createChange.getFileFormatName());
        assertEquals("CSV", createChange.getFileFormatType());
        assertEquals("\\n", createChange.getRecordDelimiter());
        assertEquals(",", createChange.getFieldDelimiter());
        assertEquals(Integer.valueOf(1), createChange.getSkipHeader());
        assertEquals("\"", createChange.getFieldOptionallyEnclosedBy());
        assertEquals("\\\\", createChange.getEscape());
        assertEquals(Boolean.TRUE, createChange.getTrimSpace());
        assertEquals("GZIP", createChange.getCompression());
        assertEquals("YYYY-MM-DD", createChange.getDateFormat());
        assertEquals("HH24:MI:SS", createChange.getTimeFormat());
        assertEquals("YYYY-MM-DD HH24:MI:SS", createChange.getTimestampFormat());
        assertEquals("HEX", createChange.getBinaryFormat());
        assertEquals("\\\\N", createChange.getNullIf());
        assertEquals("\\\\", createChange.getEscapeUnenclosedField());
        assertEquals(Boolean.FALSE, createChange.getErrorOnColumnCountMismatch());
    }

    @Test
    @DisplayName("Should handle partial file format configuration")
    void shouldHandlePartialFileFormatConfiguration() {
        // Setup partial configuration (common scenario)
        fileFormat.setFormatType("JSON");
        fileFormat.setCompression("GZIP");
        fileFormat.setDateFormat("AUTO");
        
        Change[] changes = generator.fixMissing(fileFormat, control, database, database, chain);
        
        CreateFileFormatChange createChange = (CreateFileFormatChange) changes[0];
        
        // Verify set properties are mapped
        assertEquals("TEST_FILE_FORMAT", createChange.getFileFormatName());
        assertEquals("JSON", createChange.getFileFormatType());
        assertEquals("GZIP", createChange.getCompression());
        assertEquals("AUTO", createChange.getDateFormat());
        
        // Verify unset properties are null
        assertNull(createChange.getRecordDelimiter());
        assertNull(createChange.getFieldDelimiter());
        assertNull(createChange.getSkipHeader());
        assertNull(createChange.getFieldOptionallyEnclosedBy());
        assertNull(createChange.getEscape());
        assertNull(createChange.getTrimSpace());
        assertNull(createChange.getTimeFormat());
        assertNull(createChange.getTimestampFormat());
        assertNull(createChange.getBinaryFormat());
        assertNull(createChange.getNullIf());
        assertNull(createChange.getEscapeUnenclosedField());
        assertNull(createChange.getErrorOnColumnCountMismatch());
    }

    // ==================== Different Format Types Tests ====================

    @Test
    @DisplayName("Should handle JSON format configuration")
    void shouldHandleJsonFormatConfiguration() {
        fileFormat.setFormatType("JSON");
        fileFormat.setCompression("GZIP");
        fileFormat.setDateFormat("AUTO");
        fileFormat.setTimeFormat("AUTO");
        fileFormat.setTimestampFormat("AUTO");
        
        Change[] changes = generator.fixMissing(fileFormat, control, database, database, chain);
        
        CreateFileFormatChange createChange = (CreateFileFormatChange) changes[0];
        
        assertEquals("JSON", createChange.getFileFormatType());
        assertEquals("GZIP", createChange.getCompression());
        assertEquals("AUTO", createChange.getDateFormat());
        assertEquals("AUTO", createChange.getTimeFormat());
        assertEquals("AUTO", createChange.getTimestampFormat());
    }

    @Test
    @DisplayName("Should handle PARQUET format configuration")
    void shouldHandleParquetFormatConfiguration() {
        fileFormat.setFormatType("PARQUET");
        fileFormat.setCompression("SNAPPY");
        fileFormat.setBinaryFormat("HEX");
        
        Change[] changes = generator.fixMissing(fileFormat, control, database, database, chain);
        
        CreateFileFormatChange createChange = (CreateFileFormatChange) changes[0];
        
        assertEquals("PARQUET", createChange.getFileFormatType());
        assertEquals("SNAPPY", createChange.getCompression());
        assertEquals("HEX", createChange.getBinaryFormat());
    }

    @Test
    @DisplayName("Should handle XML format configuration")
    void shouldHandleXmlFormatConfiguration() {
        fileFormat.setFormatType("XML");
        fileFormat.setCompression("BROTLI");
        fileFormat.setDateFormat("YYYY-MM-DD");
        
        Change[] changes = generator.fixMissing(fileFormat, control, database, database, chain);
        
        CreateFileFormatChange createChange = (CreateFileFormatChange) changes[0];
        
        assertEquals("XML", createChange.getFileFormatType());
        assertEquals("BROTLI", createChange.getCompression());
        assertEquals("YYYY-MM-DD", createChange.getDateFormat());
    }

    // ==================== Interface Method Tests ====================

    @Test
    @DisplayName("Should return unchanged changes for fixSchema")
    void shouldReturnUnchangedChangesForFixSchema() {
        Change[] inputChanges = {mock(CreateFileFormatChange.class)};
        Change[] result = generator.fixSchema(inputChanges, null);
        
        assertSame(inputChanges, result);
    }

    @Test
    @DisplayName("Should return unchanged changes for fixOutputAsSchema")
    void shouldReturnUnchangedChangesForFixOutputAsSchema() {
        Change[] inputChanges = {mock(CreateFileFormatChange.class)};
        Change[] result = generator.fixOutputAsSchema(inputChanges, null);
        
        assertSame(inputChanges, result);
    }

    @Test
    @DisplayName("Should return null for runBeforeTypes")
    void shouldReturnNullForRunBeforeTypes() {
        assertNull(generator.runBeforeTypes());
    }

    @Test
    @DisplayName("Should return null for runAfterTypes")
    void shouldReturnNullForRunAfterTypes() {
        assertNull(generator.runAfterTypes());
    }

    // ==================== Edge Cases and Error Handling ====================

    @Test
    @DisplayName("Should handle null FileFormat gracefully")
    void shouldHandleNullFileFormatGracefully() {
        assertThrows(NullPointerException.class, () -> {
            generator.fixMissing(null, control, database, database, chain);
        });
    }

    @Test
    @DisplayName("Should handle FileFormat with null name")
    void shouldHandleFileFormatWithNullName() {
        fileFormat.setName(null);
        
        Change[] changes = generator.fixMissing(fileFormat, control, database, database, chain);
        
        CreateFileFormatChange createChange = (CreateFileFormatChange) changes[0];
        assertNull(createChange.getFileFormatName());
    }

    @Test
    @DisplayName("Should handle FileFormat with empty name")
    void shouldHandleFileFormatWithEmptyName() {
        fileFormat.setName("");
        
        Change[] changes = generator.fixMissing(fileFormat, control, database, database, chain);
        
        CreateFileFormatChange createChange = (CreateFileFormatChange) changes[0];
        assertEquals("", createChange.getFileFormatName());
    }

    @Test
    @DisplayName("Should handle all null properties")
    void shouldHandleAllNullProperties() {
        // All properties are null by default after construction
        Change[] changes = generator.fixMissing(fileFormat, control, database, database, chain);
        
        CreateFileFormatChange createChange = (CreateFileFormatChange) changes[0];
        
        assertEquals("TEST_FILE_FORMAT", createChange.getFileFormatName());
        assertNull(createChange.getFileFormatType());
        assertNull(createChange.getRecordDelimiter());
        assertNull(createChange.getFieldDelimiter());
        assertNull(createChange.getSkipHeader());
        assertNull(createChange.getFieldOptionallyEnclosedBy());
        assertNull(createChange.getEscape());
        assertNull(createChange.getTrimSpace());
        assertNull(createChange.getCompression());
        assertNull(createChange.getDateFormat());
        assertNull(createChange.getTimeFormat());
        assertNull(createChange.getTimestampFormat());
        assertNull(createChange.getBinaryFormat());
        assertNull(createChange.getNullIf());
        assertNull(createChange.getEscapeUnenclosedField());
        assertNull(createChange.getErrorOnColumnCountMismatch());
    }

    // ==================== Boolean Property Handling Tests ====================

    @Test
    @DisplayName("Should handle boolean true values correctly")
    void shouldHandleBooleanTrueValuesCorrectly() {
        fileFormat.setTrimSpace(true);
        fileFormat.setErrorOnColumnCountMismatch(true);
        
        Change[] changes = generator.fixMissing(fileFormat, control, database, database, chain);
        
        CreateFileFormatChange createChange = (CreateFileFormatChange) changes[0];
        assertEquals(Boolean.TRUE, createChange.getTrimSpace());
        assertEquals(Boolean.TRUE, createChange.getErrorOnColumnCountMismatch());
    }

    @Test
    @DisplayName("Should handle boolean false values correctly")
    void shouldHandleBooleanFalseValuesCorrectly() {
        fileFormat.setTrimSpace(false);
        fileFormat.setErrorOnColumnCountMismatch(false);
        
        Change[] changes = generator.fixMissing(fileFormat, control, database, database, chain);
        
        CreateFileFormatChange createChange = (CreateFileFormatChange) changes[0];
        assertEquals(Boolean.FALSE, createChange.getTrimSpace());
        assertEquals(Boolean.FALSE, createChange.getErrorOnColumnCountMismatch());
    }

    // ==================== Numeric Property Handling Tests ====================

    @Test
    @DisplayName("Should handle various skip header values")
    void shouldHandleVariousSkipHeaderValues() {
        Integer[] skipHeaderValues = {0, 1, 5, 10, 100};
        
        for (Integer skipHeaderValue : skipHeaderValues) {
            fileFormat.setSkipHeader(skipHeaderValue);
            
            Change[] changes = generator.fixMissing(fileFormat, control, database, database, chain);
            
            CreateFileFormatChange createChange = (CreateFileFormatChange) changes[0];
            assertEquals(skipHeaderValue, createChange.getSkipHeader(),
                         "Skip header value should be set correctly: " + skipHeaderValue);
        }
    }

    @Test
    @DisplayName("Should handle zero skip header value")
    void shouldHandleZeroSkipHeaderValue() {
        fileFormat.setSkipHeader(0);
        
        Change[] changes = generator.fixMissing(fileFormat, control, database, database, chain);
        
        CreateFileFormatChange createChange = (CreateFileFormatChange) changes[0];
        assertEquals(Integer.valueOf(0), createChange.getSkipHeader());
    }
}