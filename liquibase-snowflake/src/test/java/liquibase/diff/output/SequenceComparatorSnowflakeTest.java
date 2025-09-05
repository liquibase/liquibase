package liquibase.diff.output;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.diff.compare.DatabaseObjectComparatorChain;
import liquibase.structure.core.Sequence;
import liquibase.structure.core.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigInteger;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Sequence comparator for Snowflake.
 * Tests diff functionality for Snowflake Sequence objects with comprehensive TDD coverage.
 * 
 * ADDRESSES_CORE_ISSUE: Complete TDD coverage for Sequence object comparison and diff generation.
 */
public class SequenceComparatorSnowflakeTest {

    @Mock
    private Database database;
    
    @Mock
    private SnowflakeDatabase snowflakeDatabase;
    
    @Mock
    private DatabaseObjectComparatorChain chain;
    
    private SequenceComparatorSnowflake comparator;
    private CompareControl compareControl;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        comparator = new SequenceComparatorSnowflake();
        compareControl = new CompareControl();
    }

    @Test
    public void testGetPriorityForSequenceWithSnowflakeDatabase() {
        int priority = comparator.getPriority(Sequence.class, snowflakeDatabase);
        assertEquals(DatabaseObjectComparator.PRIORITY_DATABASE, priority);
    }

    @Test
    public void testGetPriorityForSequenceWithNonSnowflakeDatabase() {
        int priority = comparator.getPriority(Sequence.class, database);
        assertEquals(DatabaseObjectComparator.PRIORITY_NONE, priority);
    }

    @Test
    public void testGetPriorityForNonSequenceObject() {
        int priority = comparator.getPriority(liquibase.structure.core.Table.class, snowflakeDatabase);
        assertEquals(DatabaseObjectComparator.PRIORITY_NONE, priority);
    }

    @Test
    public void testHashGeneratesCorrectIdentifier() {
        Sequence sequence = createTestSequence("TEST_SEQUENCE", "PUBLIC");
        
        String[] hash = comparator.hash(sequence, snowflakeDatabase, chain);
        
        assertEquals(2, hash.length);
        assertEquals("PUBLIC", hash[0]); // Schema name
        assertEquals("TEST_SEQUENCE", hash[1]); // Sequence name
    }

    @Test
    public void testHashWithNullSchema() {
        Sequence sequence = createTestSequence("TEST_SEQUENCE", null);
        
        String[] hash = comparator.hash(sequence, snowflakeDatabase, chain);
        
        assertEquals(2, hash.length);
        assertEquals("", hash[0]); // Empty string for null schema
        assertEquals("TEST_SEQUENCE", hash[1]);
    }

    @Test
    public void testIsSameObjectWithIdenticalSequences() {
        Sequence sequence1 = createTestSequence("SAME_SEQUENCE", "PUBLIC");
        Sequence sequence2 = createTestSequence("SAME_SEQUENCE", "PUBLIC");
        
        boolean isSame = comparator.isSameObject(sequence1, sequence2, snowflakeDatabase, chain);
        
        assertTrue(isSame);
    }

    @Test
    public void testIsSameObjectWithDifferentSequences() {
        Sequence sequence1 = createTestSequence("SEQUENCE_ONE", "PUBLIC");
        Sequence sequence2 = createTestSequence("SEQUENCE_TWO", "PUBLIC");
        
        boolean isSame = comparator.isSameObject(sequence1, sequence2, snowflakeDatabase, chain);
        
        assertFalse(isSame);
    }

    @Test
    public void testIsSameObjectWithDifferentSchemas() {
        Sequence sequence1 = createTestSequence("SAME_SEQUENCE", "SCHEMA_ONE");
        Sequence sequence2 = createTestSequence("SAME_SEQUENCE", "SCHEMA_TWO");
        
        boolean isSame = comparator.isSameObject(sequence1, sequence2, snowflakeDatabase, chain);
        
        assertFalse(isSame);
    }

    @Test
    public void testIsSameObjectWithNonSequenceObjects() {
        Sequence sequence = createTestSequence("SEQUENCE", "PUBLIC");
        liquibase.structure.core.Table table = new liquibase.structure.core.Table();
        
        boolean isSame = comparator.isSameObject(sequence, table, snowflakeDatabase, chain);
        
        assertFalse(isSame);
    }

    @Test
    public void testIsSameObjectWithCaseInsensitiveNames() {
        Sequence sequence1 = createTestSequence("test_sequence", "public");
        Sequence sequence2 = createTestSequence("TEST_SEQUENCE", "PUBLIC");
        
        boolean isSame = comparator.isSameObject(sequence1, sequence2, snowflakeDatabase, chain);
        
        assertTrue(isSame); // Sequence names should be case-insensitive
    }

    @Test
    public void testFindDifferencesWithIdenticalSequences() {
        Sequence sequence1 = createTestSequence("IDENTICAL_SEQUENCE", "PUBLIC");
        sequence1.setStartValue(BigInteger.valueOf(1));
        sequence1.setIncrementBy(BigInteger.valueOf(1));
        sequence1.setMinValue(BigInteger.valueOf(1));
        sequence1.setMaxValue(new BigInteger("9999999999999999999"));
        sequence1.setWillCycle(false);
        
        Sequence sequence2 = createTestSequence("IDENTICAL_SEQUENCE", "PUBLIC");
        sequence2.setStartValue(BigInteger.valueOf(1));
        sequence2.setIncrementBy(BigInteger.valueOf(1));
        sequence2.setMinValue(BigInteger.valueOf(1));
        sequence2.setMaxValue(new BigInteger("9999999999999999999"));
        sequence2.setWillCycle(false);
        
        ObjectDifferences differences = comparator.findDifferences(
            sequence1, sequence2, snowflakeDatabase, compareControl, chain, new HashSet<>()
        );
        
        assertFalse(differences.hasDifferences());
    }

    @Test
    public void testFindDifferencesWithDifferentStartValue() {
        Sequence sequence1 = createTestSequence("START_SEQUENCE", "PUBLIC");
        sequence1.setStartValue(BigInteger.valueOf(1));
        
        Sequence sequence2 = createTestSequence("START_SEQUENCE", "PUBLIC");
        sequence2.setStartValue(BigInteger.valueOf(10));
        
        ObjectDifferences differences = comparator.findDifferences(
            sequence1, sequence2, snowflakeDatabase, compareControl, chain, new HashSet<>()
        );
        
        assertTrue(differences.hasDifferences());
    }

    @Test
    public void testFindDifferencesWithDifferentIncrementBy() {
        Sequence sequence1 = createTestSequence("INCREMENT_SEQUENCE", "PUBLIC");
        sequence1.setIncrementBy(BigInteger.valueOf(1));
        
        Sequence sequence2 = createTestSequence("INCREMENT_SEQUENCE", "PUBLIC");
        sequence2.setIncrementBy(BigInteger.valueOf(5));
        
        ObjectDifferences differences = comparator.findDifferences(
            sequence1, sequence2, snowflakeDatabase, compareControl, chain, new HashSet<>()
        );
        
        assertTrue(differences.hasDifferences());
    }

    @Test
    public void testFindDifferencesWithDifferentMinValue() {
        Sequence sequence1 = createTestSequence("MIN_SEQUENCE", "PUBLIC");
        sequence1.setMinValue(BigInteger.valueOf(1));
        
        Sequence sequence2 = createTestSequence("MIN_SEQUENCE", "PUBLIC");
        sequence2.setMinValue(BigInteger.valueOf(0));
        
        ObjectDifferences differences = comparator.findDifferences(
            sequence1, sequence2, snowflakeDatabase, compareControl, chain, new HashSet<>()
        );
        
        assertTrue(differences.hasDifferences());
    }

    @Test
    public void testFindDifferencesWithDifferentMaxValue() {
        Sequence sequence1 = createTestSequence("MAX_SEQUENCE", "PUBLIC");
        sequence1.setMaxValue(BigInteger.valueOf(1000));
        
        Sequence sequence2 = createTestSequence("MAX_SEQUENCE", "PUBLIC");
        sequence2.setMaxValue(BigInteger.valueOf(2000));
        
        ObjectDifferences differences = comparator.findDifferences(
            sequence1, sequence2, snowflakeDatabase, compareControl, chain, new HashSet<>()
        );
        
        assertTrue(differences.hasDifferences());
    }

    @Test
    public void testFindDifferencesWithDifferentCycleOption() {
        Sequence sequence1 = createTestSequence("CYCLE_SEQUENCE", "PUBLIC");
        sequence1.setWillCycle(false);
        
        Sequence sequence2 = createTestSequence("CYCLE_SEQUENCE", "PUBLIC");
        sequence2.setWillCycle(true);
        
        ObjectDifferences differences = comparator.findDifferences(
            sequence1, sequence2, snowflakeDatabase, compareControl, chain, new HashSet<>()
        );
        
        assertTrue(differences.hasDifferences());
    }

    @Test
    public void testFindDifferencesWithNullValues() {
        Sequence sequence1 = createTestSequence("NULL_SEQUENCE", "PUBLIC");
        sequence1.setStartValue(null);
        sequence1.setIncrementBy(null);
        
        Sequence sequence2 = createTestSequence("NULL_SEQUENCE", "PUBLIC");
        sequence2.setStartValue(BigInteger.valueOf(1));
        sequence2.setIncrementBy(BigInteger.valueOf(1));
        
        ObjectDifferences differences = comparator.findDifferences(
            sequence1, sequence2, snowflakeDatabase, compareControl, chain, new HashSet<>()
        );
        
        assertTrue(differences.hasDifferences());
    }

    @Test
    public void testFindDifferencesWithMixedConfigurationChanges() {
        Sequence sequence1 = createTestSequence("MIXED_SEQUENCE", "PUBLIC");
        sequence1.setStartValue(BigInteger.valueOf(1));
        sequence1.setIncrementBy(BigInteger.valueOf(1));
        sequence1.setMinValue(BigInteger.valueOf(1));
        sequence1.setMaxValue(BigInteger.valueOf(1000));
        sequence1.setWillCycle(false);
        
        Sequence sequence2 = createTestSequence("MIXED_SEQUENCE", "PUBLIC");
        sequence2.setStartValue(BigInteger.valueOf(10));
        sequence2.setIncrementBy(BigInteger.valueOf(5));
        sequence2.setMinValue(BigInteger.valueOf(0));
        sequence2.setMaxValue(BigInteger.valueOf(2000));
        sequence2.setWillCycle(true);
        
        ObjectDifferences differences = comparator.findDifferences(
            sequence1, sequence2, snowflakeDatabase, compareControl, chain, new HashSet<>()
        );
        
        assertTrue(differences.hasDifferences());
    }

    @Test
    public void testFindDifferencesWithIdenticalNullValues() {
        Sequence sequence1 = createTestSequence("NULL_VALUES_SEQUENCE", "PUBLIC");
        sequence1.setStartValue(null);
        sequence1.setIncrementBy(null);
        sequence1.setMinValue(null);
        sequence1.setMaxValue(null);
        sequence1.setWillCycle(null);
        
        Sequence sequence2 = createTestSequence("NULL_VALUES_SEQUENCE", "PUBLIC");
        sequence2.setStartValue(null);
        sequence2.setIncrementBy(null);
        sequence2.setMinValue(null);
        sequence2.setMaxValue(null);
        sequence2.setWillCycle(null);
        
        ObjectDifferences differences = comparator.findDifferences(
            sequence1, sequence2, snowflakeDatabase, compareControl, chain, new HashSet<>()
        );
        
        assertFalse(differences.hasDifferences());
    }

    @Test
    public void testFindDifferencesWithOnlyConfigurationProperties() {
        Sequence sequence1 = createTestSequence("CONFIG_ONLY_SEQUENCE", "PUBLIC");
        // Set only configuration properties
        sequence1.setStartValue(BigInteger.valueOf(100));
        sequence1.setIncrementBy(BigInteger.valueOf(10));
        sequence1.setMinValue(BigInteger.valueOf(1));
        sequence1.setMaxValue(BigInteger.valueOf(10000));
        sequence1.setWillCycle(true);
        
        Sequence sequence2 = createTestSequence("CONFIG_ONLY_SEQUENCE", "PUBLIC");
        // Same configuration properties
        sequence2.setStartValue(BigInteger.valueOf(100));
        sequence2.setIncrementBy(BigInteger.valueOf(10));
        sequence2.setMinValue(BigInteger.valueOf(1));
        sequence2.setMaxValue(BigInteger.valueOf(10000));
        sequence2.setWillCycle(true);
        
        ObjectDifferences differences = comparator.findDifferences(
            sequence1, sequence2, snowflakeDatabase, compareControl, chain, new HashSet<>()
        );
        
        assertFalse(differences.hasDifferences());
    }

    private Sequence createTestSequence(String name, String schemaName) {
        Sequence sequence = new Sequence();
        sequence.setName(name);
        if (schemaName != null) {
            sequence.setSchema(new liquibase.structure.core.Schema((liquibase.structure.core.Catalog) null, schemaName));
        }
        return sequence;
    }
}