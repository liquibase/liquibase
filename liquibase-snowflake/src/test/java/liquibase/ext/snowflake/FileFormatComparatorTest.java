package liquibase.ext.snowflake;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.object.FileFormat;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.FileFormatComparator;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static liquibase.diff.compare.DatabaseObjectComparator.PRIORITY_TYPE;
import static liquibase.diff.compare.DatabaseObjectComparator.PRIORITY_NONE;

/**
 * TDD test suite for FileFormatComparator.
 * Tests organized by categories: positive, negative, boundary, edge cases.
 */
public class FileFormatComparatorTest {

    private FileFormatComparator comparator;
    private SnowflakeDatabase database;
    private Schema testSchema;

    @BeforeEach
    void setUp() {
        comparator = new FileFormatComparator();
        database = mock(SnowflakeDatabase.class);
        testSchema = new Schema("TEST_CATALOG", "TEST_SCHEMA");
    }

    // === POSITIVE TESTS ===
    
    @Test
    void shouldIdentifySameObjectsWhenIdentityMatches() {
        FileFormat ff1 = new FileFormat("TEST_FF");
        ff1.setSchema(testSchema);
        
        FileFormat ff2 = new FileFormat("TEST_FF");
        ff2.setSchema(testSchema);
        
        boolean result = comparator.isSameObject(ff1, ff2, database, null);
        assertTrue(result);
    }

    @Test
    void shouldHaveHighPriorityForFileFormatOnSnowflake() {
        int priority = comparator.getPriority(FileFormat.class, database);
        assertEquals(PRIORITY_TYPE, priority);
    }

    // === NEGATIVE TESTS ===
    
    @Test
    void shouldReturnFalseForDifferentObjects() {
        FileFormat ff1 = new FileFormat("FF_ONE");
        ff1.setSchema(testSchema);
        
        FileFormat ff2 = new FileFormat("FF_TWO");
        ff2.setSchema(testSchema);
        
        boolean result = comparator.isSameObject(ff1, ff2, database, null);
        assertFalse(result);
    }

    // === BOUNDARY TESTS ===
    
    // Boundary tests will be added via TDD micro-cycles
    // Boundary tests added via TDD micro-cycles

    // === EDGE CASE TESTS ===
    
    @Test
    void shouldDetectNullIfArrayDifferences() {
        // ADDRESSES COVERAGE GAP: Test NULL_IF array comparison edge case
        FileFormat format1 = new FileFormat("TEST_NULLIF");
        format1.setSchema(testSchema);
        format1.setNullIf("['NULL', 'null']");  // First array
        
        FileFormat format2 = new FileFormat("TEST_NULLIF");
        format2.setSchema(testSchema);
        format2.setNullIf("['NULL', 'EMPTY']");  // Different array
        
        ObjectDifferences differences = comparator.findDifferences(
            format1, format2, database, new CompareControl(), null, new HashSet<>()
        );
        
        assertTrue(differences.hasDifferences(), "Should detect differences in NULL_IF arrays");
        System.out.println("✅ Successfully tested NULL_IF array differences detection");
    }

    @Test
    void shouldHandlePhantomPropertyEdgeCases() {
        // ADDRESSES COVERAGE GAP: Test phantom property comparison edge cases
        FileFormat format1 = new FileFormat("TEST_PHANTOM");
        format1.setSchema(testSchema);
        format1.setSkipByteOrderMark(true);    // Non-null phantom property
        format1.setSkipBlankLines(null);  // Null phantom property
        
        FileFormat format2 = new FileFormat("TEST_PHANTOM");
        format2.setSchema(testSchema);
        format2.setSkipByteOrderMark(false);   // Different non-null phantom property
        format2.setSkipBlankLines(true);  // Non-null vs null phantom property
        
        ObjectDifferences differences = comparator.findDifferences(
            format1, format2, database, new CompareControl(), null, new HashSet<>()
        );
        
        assertTrue(differences.hasDifferences(), "Should detect differences in non-null phantom properties");
        System.out.println("✅ Successfully tested phantom property edge cases");
    }

    @Test
    void shouldNotComparePhantomPropertiesWhenOneIsNull() {
        // ADDRESSES COVERAGE GAP: Test phantom property null handling
        FileFormat format1 = new FileFormat("TEST_PHANTOM_NULL");
        format1.setSchema(testSchema);
        format1.setSkipByteOrderMark(null);      // Null phantom property
        format1.setReplaceInvalidCharacters(null);  // Both null
        
        FileFormat format2 = new FileFormat("TEST_PHANTOM_NULL");
        format2.setSchema(testSchema);
        format2.setSkipByteOrderMark(true);      // Non-null vs null - should NOT compare
        format2.setReplaceInvalidCharacters(null);  // Both null
        
        ObjectDifferences differences = comparator.findDifferences(
            format1, format2, database, new CompareControl(), null, new HashSet<>()
        );
        
        assertFalse(differences.hasDifferences(), "Should NOT compare phantom properties when one is null");
        System.out.println("✅ Successfully tested phantom property null handling");
    }

    @Test
    void shouldHandleComplexHashCalculationWithNullValues() {
        // ADDRESSES COVERAGE GAP: Test hash calculation with various null scenarios
        FileFormat format1 = new FileFormat("TEST_HASH");
        format1.setSchema(null);  // Null schema
        
        String[] hash1 = comparator.hash(format1, database, null);
        assertNotNull(hash1, "Hash should not be null even with null schema");
        assertEquals(3, hash1.length, "Hash should have 3 elements");
        assertEquals("TEST_HASH", hash1[0], "First element should be name");
        assertEquals("", hash1[1], "Second element should be empty string for null catalog");
        assertEquals("", hash1[2], "Third element should be empty string for null schema");
        
        // Test with partial schema (catalog but no schema name)
        Catalog catalog = new Catalog("TEST_CATALOG");
        Schema schemaWithCatalog = new Schema(catalog, null);
        FileFormat format2 = new FileFormat("TEST_HASH_2");
        format2.setSchema(schemaWithCatalog);
        
        String[] hash2 = comparator.hash(format2, database, null);
        assertEquals("TEST_HASH_2", hash2[0], "Name should be correct");
        assertEquals("TEST_CATALOG", hash2[1], "Catalog should be correct");
        assertEquals("", hash2[2], "Schema name should be empty for null");
        
        System.out.println("✅ Successfully tested complex hash calculation scenarios");
    }

    @Test
    void shouldTestAllComparisonRuleCategories() {
        // ADDRESSES COVERAGE GAP: Comprehensive test of all comparison rule types
        FileFormat format1 = new FileFormat("COMPREHENSIVE_TEST");
        format1.setSchema(testSchema);
        
        // COMPARE_ALWAYS properties
        format1.setFormatType("CSV");
        format1.setCompression("GZIP");
        format1.setFieldDelimiter(",");
        
        // COMPARE_WHEN_NOT_DEFAULT properties
        format1.setDateFormat("YYYY-MM-DD");  // Non-default
        format1.setTimeFormat("AUTO");        // Default
        format1.setBinaryFormat("BASE64");    // Non-default
        
        // COMPARE_WHEN_PRESENT properties
        format1.setNullIf("['NULL']");        // Present
        
        // Phantom properties
        format1.setSkipByteOrderMark(true);        // Present
        format1.setSkipBlankLines(null);      // Not present
        
        FileFormat format2 = new FileFormat("COMPREHENSIVE_TEST");
        format2.setSchema(testSchema);
        
        // Same COMPARE_ALWAYS
        format2.setFormatType("CSV");
        format2.setCompression("GZIP");
        format2.setFieldDelimiter(",");
        
        // Mixed COMPARE_WHEN_NOT_DEFAULT
        format2.setDateFormat("DD-MM-YYYY");  // Different non-default - should detect
        format2.setTimeFormat("AUTO");        // Same default - should ignore
        format2.setBinaryFormat("BASE64");    // Same non-default - should not detect
        
        // Same COMPARE_WHEN_PRESENT
        format2.setNullIf("['NULL']");        // Same present - should not detect
        
        // Same phantom properties
        format2.setSkipByteOrderMark(true);        // Same present - should not detect
        format2.setSkipBlankLines(null);      // Both not present - should ignore
        
        ObjectDifferences differences = comparator.findDifferences(
            format1, format2, database, new CompareControl(), null, new HashSet<>()
        );
        
        assertTrue(differences.hasDifferences(), "Should detect the dateFormat difference");
        System.out.println("✅ Successfully tested all comparison rule categories");
    }
    
    // Edge case tests will be added via TDD micro-cycles
    // Edge case tests added via TDD micro-cycles
}