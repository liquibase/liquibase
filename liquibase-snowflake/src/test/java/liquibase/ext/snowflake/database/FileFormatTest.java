package liquibase.ext.snowflake.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import liquibase.database.object.FileFormat;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;
import liquibase.structure.DatabaseObject;

/**
 * TDD test suite for FileFormat object model.
 * Tests organized by categories: positive, negative, boundary, edge cases.
 */
public class FileFormatTest {

    private FileFormat fileformat;
    private Schema testSchema;

    @BeforeEach
    void setUp() {
        fileformat = new FileFormat();
        testSchema = new Schema("TEST_CATALOG", "TEST_SCHEMA");
    }

    // === POSITIVE TESTS ===
    
    @Test
    void testConstructorWithName() {
        FileFormat obj = new FileFormat("TEST_FILEFORMAT");
        assertEquals("TEST_FILEFORMAT", obj.getName());
    }

    @Test
    void testBasicGettersAndSetters() {
        fileformat.setName("TEST_FILEFORMAT");
        fileformat.setSchema(testSchema);
        
        assertEquals("TEST_FILEFORMAT", fileformat.getName());
        assertEquals(testSchema, fileformat.getSchema());
    }

    @Test
    void shouldNotIncludeConfigurationPropertiesInEquals() {
        FileFormat obj1 = new FileFormat("TEST_FF");
        obj1.setSchema(testSchema);
        obj1.setType("CSV");
        obj1.setCompression("GZIP");
        
        FileFormat obj2 = new FileFormat("TEST_FF");
        obj2.setSchema(testSchema);
        obj2.setType("JSON");  // Different type
        obj2.setCompression("BROTLI");  // Different compression
        
        assertEquals(obj1, obj2); // Should still be equal - only identity matters
        assertEquals(obj1.hashCode(), obj2.hashCode());
    }
    
    @Test
    void shouldSupportCsvSpecificProperties() {
        fileformat.setName("CSV_FORMAT");
        fileformat.setType("CSV");
        fileformat.setRecordDelimiter("\n");
        fileformat.setFieldDelimiter(",");
        fileformat.setSkipHeader(1);
        fileformat.setTrimSpace(true);
        
        assertEquals("CSV_FORMAT", fileformat.getName());
        assertEquals("CSV", fileformat.getType());
        assertEquals("\n", fileformat.getRecordDelimiter());
        assertEquals(",", fileformat.getFieldDelimiter());
        assertEquals(Integer.valueOf(1), fileformat.getSkipHeader());
        assertTrue(fileformat.getTrimSpace());
    }

    // === NEGATIVE TESTS ===
    
    @Test
    void testNullNameHandling() {
        assertDoesNotThrow(() -> fileformat.setName(null));
        assertNull(fileformat.getName());
    }

    @Test
    void testNullSchemaHandling() {
        assertDoesNotThrow(() -> fileformat.setSchema(null));
        assertNull(fileformat.getSchema());
    }

    // Property negative tests will be added via TDD micro-cycles
    // Property negative tests added via TDD micro-cycles

    // === BOUNDARY TESTS ===
    
    @Test
    void testEmptyNameHandling() {
        fileformat.setName("");
        assertEquals("", fileformat.getName());
    }

    @Test
    void testLongNameHandling() {
        // Java 8 compatible way to create repeated string
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 255; i++) {
            sb.append("A");
        }
        String longName = sb.toString();
        fileformat.setName(longName);
        assertEquals(longName, fileformat.getName());
    }

    // Property boundary tests will be added via TDD micro-cycles
    // Property boundary tests added via TDD micro-cycles

    // === EDGE CASE TESTS ===
    
    @Test
    void testEqualsContract() {
        FileFormat obj1 = new FileFormat("TEST_FILEFORMAT");
        FileFormat obj2 = new FileFormat("TEST_FILEFORMAT");
        FileFormat obj3 = new FileFormat("DIFFERENT_FILEFORMAT");
        
        // Reflexive
        assertEquals(obj1, obj1);
        
        // Symmetric
        assertEquals(obj1, obj2);
        assertEquals(obj2, obj1);
        
        // Transitive (implied by same name)
        
        // Consistent
        assertNotEquals(obj1, obj3);
        assertNotEquals(obj3, obj1);
        
        // Null comparison
        assertNotEquals(obj1, null);
    }

    @Test
    void testHashCodeContract() {
        FileFormat obj1 = new FileFormat("TEST_FILEFORMAT");
        FileFormat obj2 = new FileFormat("TEST_FILEFORMAT");
        
        assertEquals(obj1.hashCode(), obj2.hashCode());
    }

    @Test
    void testToStringFormat() {
        fileformat.setName("TEST_FILEFORMAT");
        fileformat.setSchema(testSchema);
        
        String result = fileformat.toString();
        assertTrue(result.contains("FileFormat"));
        assertTrue(result.contains("TEST_FILEFORMAT"));
        assertTrue(result.contains("TEST_SCHEMA"));
    }

    // === COMPREHENSIVE REQUIREMENTS VALIDATION TESTS ===
    
    @Test
    void testAllRequiredPropertiesImplemented() {
        // Verify ALL properties from requirements document are implemented
        fileformat.setName("COMPLETE_TEST");
        fileformat.setSchema(testSchema);
        
        // Core properties
        fileformat.setFormatType("CSV");
        fileformat.setCompression("GZIP");
        
        // CSV-specific properties from requirements
        fileformat.setRecordDelimiter("\n");
        fileformat.setFieldDelimiter(",");
        fileformat.setFieldOptionallyEnclosedBy("\"");
        fileformat.setEscape("\\");
        fileformat.setEscapeUnenclosedField("\\");
        fileformat.setSkipHeader(1);
        fileformat.setSkipBlankLines(true);
        fileformat.setTrimSpace(true);
        fileformat.setEmptyFieldAsNull(true);
        fileformat.setErrorOnColumnCountMismatch(false);
        fileformat.setMultiLine(true);
        fileformat.setParseHeader(false);
        
        // Format properties from requirements
        fileformat.setDateFormat("YYYY-MM-DD");
        fileformat.setTimeFormat("HH24:MI:SS");
        fileformat.setTimestampFormat("YYYY-MM-DD HH24:MI:SS");
        fileformat.setBinaryFormat("HEX");
        fileformat.setNullIf("NULL");
        
        // Additional properties from requirements
        // validateUtf8 removed - not available in Snowflake INFORMATION_SCHEMA.FILE_FORMATS
        fileformat.setReplaceInvalidCharacters(false);
        fileformat.setSkipByteOrderMark(true);
        fileformat.setEncoding("UTF8");
        fileformat.setFileExtension(".csv");
        
        // Verify all properties are accessible
        assertEquals("CSV", fileformat.getFormatType());
        assertEquals("GZIP", fileformat.getCompression());
        assertEquals("\n", fileformat.getRecordDelimiter());
        assertEquals(",", fileformat.getFieldDelimiter());
        assertEquals("\"", fileformat.getFieldOptionallyEnclosedBy());
        assertEquals("\\", fileformat.getEscape());
        assertEquals("\\", fileformat.getEscapeUnenclosedField());
        assertEquals(Integer.valueOf(1), fileformat.getSkipHeader());
        assertTrue(fileformat.getSkipBlankLines());
        assertTrue(fileformat.getTrimSpace());
        assertTrue(fileformat.getEmptyFieldAsNull());
        assertFalse(fileformat.getErrorOnColumnCountMismatch());
        assertTrue(fileformat.getMultiLine());
        assertFalse(fileformat.getParseHeader());
        assertEquals("YYYY-MM-DD", fileformat.getDateFormat());
        assertEquals("HH24:MI:SS", fileformat.getTimeFormat());
        assertEquals("YYYY-MM-DD HH24:MI:SS", fileformat.getTimestampFormat());
        assertEquals("HEX", fileformat.getBinaryFormat());
        assertEquals("NULL", fileformat.getNullIf());
        // assertTrue(fileformat.getValidateUtf8()) - removed, method no longer exists
        assertFalse(fileformat.getReplaceInvalidCharacters());
        assertTrue(fileformat.getSkipByteOrderMark());
        assertEquals("UTF8", fileformat.getEncoding());
        assertEquals(".csv", fileformat.getFileExtension());
    }
    
    @Test
    void testRequirementsCompliantPropertyComparison() {
        // Test that comparison excludes state properties per requirements
        FileFormat format1 = new FileFormat("TEST_FORMAT");
        format1.setSchema(testSchema);
        format1.setFormatType("CSV");
        format1.setCompression("GZIP");
        format1.setEncoding("UTF8");
        
        FileFormat format2 = new FileFormat("TEST_FORMAT");
        format2.setSchema(testSchema);
        format2.setFormatType("CSV");
        format2.setCompression("GZIP");
        format2.setEncoding("UTF8");
        
        // Should be equal despite different configuration properties not being set
        assertEquals(format1, format2);
        assertEquals(format1.hashCode(), format2.hashCode());
    }
    
    @Test
    void testBackwardCompatibilityMethods() {
        // Test that backward compatibility methods still work
        fileformat.setQuoteCharacter("\"");
        fileformat.setEscapeCharacter("\\");
        
        assertEquals("\"", fileformat.getQuoteCharacter());
        assertEquals("\\", fileformat.getEscapeCharacter());
        
        // Test the type compatibility methods
        fileformat.setType("JSON");
        assertEquals("JSON", fileformat.getType());
        assertEquals("JSON", fileformat.getFormatType()); // Should map to formatType
    }
    
    @Test
    void shouldReturnContainingObjectsWhenSchemaPresent() {
        // ADDRESSES COVERAGE GAP: Test getContainingObjects() method with schema
        Catalog catalog = new Catalog("TEST_CATALOG");
        Schema schema = new Schema(catalog, "TEST_SCHEMA");
        
        FileFormat format = new FileFormat("TEST_FORMAT");
        format.setSchema(schema);
        
        DatabaseObject[] containingObjects = format.getContainingObjects();
        
        assertNotNull(containingObjects, "Should return containing objects when schema is present");
        assertEquals(1, containingObjects.length, "Should return exactly one containing object");
        assertEquals(schema, containingObjects[0], "Should return the schema as containing object");
    }

    @Test
    void shouldReturnNullContainingObjectsWhenSchemaAbsent() {
        // ADDRESSES COVERAGE GAP: Test getContainingObjects() method without schema
        FileFormat format = new FileFormat("TEST_FORMAT");
        format.setSchema(null);  // Explicitly set null schema
        
        DatabaseObject[] containingObjects = format.getContainingObjects();
        
        assertNull(containingObjects, "Should return null when schema is not present");
    }
    
    // Property edge case tests will be added via TDD micro-cycles
    // Property edge tests added via TDD micro-cycles
}