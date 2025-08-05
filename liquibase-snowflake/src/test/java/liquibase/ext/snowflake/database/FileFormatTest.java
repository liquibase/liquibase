package liquibase.ext.snowflake.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import liquibase.database.object.FileFormat;
import liquibase.structure.core.Schema;

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

    // Property edge case tests will be added via TDD micro-cycles
    // Property edge tests added via TDD micro-cycles
}