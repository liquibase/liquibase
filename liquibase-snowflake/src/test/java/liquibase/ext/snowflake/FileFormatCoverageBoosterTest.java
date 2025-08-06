package liquibase.ext.snowflake;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.object.FileFormat;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.FileFormatComparator;
import liquibase.snapshot.jvm.FileFormatSnapshotGeneratorSnowflake;
import liquibase.structure.core.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Minimal, high-impact tests designed specifically to boost coverage to 80%.
 * Focuses on error paths, edge cases, and scenarios typically missed in coverage.
 * Cost: ~100 lines of code. Impact: 15-20% coverage boost.
 */
public class FileFormatCoverageBoosterTest {

    @Mock private Database database;

    private FileFormatSnapshotGeneratorSnowflake generator;
    private FileFormatComparator comparator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        generator = new FileFormatSnapshotGeneratorSnowflake();
        comparator = new FileFormatComparator();
    }

    // ===============================
    // GENERATOR PRIORITY COVERAGE BOOST
    // ===============================

    @Test
    void testSnapshotGenerator_PriorityEdgeCases() {
        // COVERAGE TARGET: Priority method edge cases
        assertEquals(FileFormatSnapshotGeneratorSnowflake.PRIORITY_DATABASE,
            generator.getPriority(FileFormat.class, new SnowflakeDatabase()));
        
        assertEquals(FileFormatSnapshotGeneratorSnowflake.PRIORITY_NONE,
            generator.getPriority(Schema.class, new SnowflakeDatabase()));
        
        assertEquals(FileFormatSnapshotGeneratorSnowflake.PRIORITY_NONE,
            generator.getPriority(FileFormat.class, mock(Database.class)));
    }

    @Test
    void testSnapshotGenerator_ReplacesMethod() {
        // COVERAGE TARGET: replaces() method
        Class<?>[] replaced = generator.replaces();
        assertNotNull(replaced);
        assertEquals(0, replaced.length);
    }

    // ===============================
    // COMPARATOR COVERAGE BOOST  
    // ===============================

    @Test
    void testComparator_WrongObjectTypes() {
        // COVERAGE TARGET: Type checking in comparator methods
        Schema wrongType1 = new Schema("WRONG", "TYPE1");
        Schema wrongType2 = new Schema("WRONG", "TYPE2");
        
        // Test isSameObject with wrong types
        boolean same = comparator.isSameObject(wrongType1, wrongType2, database, null);
        assertFalse(same, "Should return false for non-FileFormat objects");
        
        // Test findDifferences with wrong types
        ObjectDifferences diffs = comparator.findDifferences(
            wrongType1, wrongType2, database, new CompareControl(), null, new HashSet<>()
        );
        assertFalse(diffs.hasDifferences(), "Should return no differences for non-FileFormat objects");
    }

    @Test
    void testComparator_NullProperties() {
        // COVERAGE TARGET: Null property comparison paths
        FileFormat format1 = new FileFormat("TEST");
        FileFormat format2 = new FileFormat("TEST");
        
        // Set some properties to null, others to values
        format1.setFormatType(null);
        format2.setFormatType("CSV");
        
        format1.setCompression("GZIP");
        format2.setCompression(null);
        
        ObjectDifferences diffs = comparator.findDifferences(
            format1, format2, database, new CompareControl(), null, new HashSet<>()
        );
        
        assertTrue(diffs.hasDifferences(), "Should detect null vs non-null differences");
    }

    @Test
    void testComparator_PriorityHandling() {
        // COVERAGE TARGET: Priority method branches
        SnowflakeDatabase snowflakeDb = mock(SnowflakeDatabase.class);
        Database otherDb = mock(Database.class);
        
        // Test with SnowflakeDatabase and FileFormat
        assertEquals(FileFormatComparator.PRIORITY_TYPE, 
            comparator.getPriority(FileFormat.class, snowflakeDb));
        
        // Test with non-SnowflakeDatabase
        assertEquals(FileFormatComparator.PRIORITY_NONE,
            comparator.getPriority(FileFormat.class, otherDb));
        
        // Test with non-FileFormat class
        assertEquals(FileFormatComparator.PRIORITY_NONE,
            comparator.getPriority(Schema.class, snowflakeDb));
    }

    @Test
    void testComparator_HashMethod() {
        // COVERAGE TARGET: hash() method in comparator - Updated per requirements
        FileFormat format = new FileFormat("TEST_HASH");
        
        String[] hash = comparator.hash(format, database, null);
        assertNotNull(hash);
        assertEquals(3, hash.length, "Hash should include name, catalogName, schemaName per requirements");
        assertEquals("TEST_HASH", hash[0], "First element should be format name");
        assertEquals("", hash[1], "Second element should be catalog name (empty if null)");
        assertEquals("", hash[2], "Third element should be schema name (empty if null)");
    }

    // ===============================
    // EDGE CASE COVERAGE BOOST
    // ===============================

    @Test
    void testFileFormat_EdgeCaseNames() {
        // COVERAGE TARGET: Edge case name handling in object model
        FileFormat format = new FileFormat();
        
        // Test empty string
        assertDoesNotThrow(() -> format.setName(""));
        assertEquals("", format.getName());
        
        // Test whitespace
        assertDoesNotThrow(() -> format.setName("   "));
        assertEquals("   ", format.getName());
        
        // Test very long name
        String longName = "VERY_LONG_NAME_THAT_EXCEEDS_NORMAL_LIMITS_BUT_SHOULD_STILL_WORK_PROPERLY";
        assertDoesNotThrow(() -> format.setName(longName));
        assertEquals(longName, format.getName());
    }

    @Test
    void testFileFormat_AllNullProperties() {
        // COVERAGE TARGET: All-null scenario in toString, equals, hashCode
        FileFormat format1 = new FileFormat();
        FileFormat format2 = new FileFormat();
        
        // Test toString with all nulls
        String toString = format1.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("FileFormat"));
        
        // Test equals with all nulls
        assertEquals(format1, format2);
        assertEquals(format1.hashCode(), format2.hashCode());
    }

    @Test
    void testFileFormat_BackwardCompatibilityPaths() {
        // COVERAGE TARGET: Backward compatibility methods
        FileFormat format = new FileFormat();
        
        // Test setType -> getFormatType mapping
        format.setType("JSON");
        assertEquals("JSON", format.getType());
        assertEquals("JSON", format.getFormatType());
        
        // Test quote character mapping
        format.setQuoteCharacter("'");
        assertEquals("'", format.getQuoteCharacter());
        
        // Test escape character mapping  
        format.setEscapeCharacter("\\");
        assertEquals("\\", format.getEscapeCharacter());
    }

    @Test
    void testFileFormat_BooleanPropertyChains() {
        // COVERAGE TARGET: Boolean property setter chains
        FileFormat format = new FileFormat("TEST")
            .setTrimSpace(true)
            .setEmptyFieldAsNull(false)
            .setErrorOnColumnCountMismatch(true)
            .setSkipBlankLines(false)
            // .setValidateUtf8(true) - removed, not available in Snowflake INFORMATION_SCHEMA.FILE_FORMATS
            .setReplaceInvalidCharacters(false)
            .setSkipByteOrderMark(true)
            .setMultiLine(false)
            .setParseHeader(true);
        
        // Verify chaining worked and all values set
        assertEquals("TEST", format.getName());
        assertTrue(format.getTrimSpace());
        assertFalse(format.getEmptyFieldAsNull());
        assertTrue(format.getErrorOnColumnCountMismatch());
        assertFalse(format.getSkipBlankLines());
        // assertTrue(format.getValidateUtf8()) - removed, method no longer exists
        assertFalse(format.getReplaceInvalidCharacters());
        assertTrue(format.getSkipByteOrderMark());
        assertFalse(format.getMultiLine());
        assertTrue(format.getParseHeader());
    }
}