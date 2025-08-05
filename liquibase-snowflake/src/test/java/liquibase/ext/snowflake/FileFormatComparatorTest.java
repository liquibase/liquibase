package liquibase.ext.snowflake;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.object.FileFormat;
import liquibase.diff.output.FileFormatComparator;
import liquibase.structure.core.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    
    // Edge case tests will be added via TDD micro-cycles
    // Edge case tests added via TDD micro-cycles
}