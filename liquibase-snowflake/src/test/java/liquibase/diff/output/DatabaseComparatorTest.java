package liquibase.diff.output;

import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.diff.compare.DatabaseObjectComparatorChain;
import liquibase.structure.core.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * TDD Tests for Snowflake Database Comparator
 * Testing all comparison functionality based on requirements
 */
public class DatabaseComparatorTest {
    
    private DatabaseComparator comparator;
    
    @Mock
    private SnowflakeDatabase snowflakeDatabase;
    
    @Mock
    private PostgresDatabase postgresDatabase;
    
    @Mock
    private CompareControl compareControl;
    
    @Mock
    private DatabaseObjectComparatorChain chain;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        comparator = new DatabaseComparator();
    }
    
    // ===== PRIORITY AND FRAMEWORK INTEGRATION TESTS =====
    
    @Test
    void testGetPriorityReturnsCorrectValues() {
        // Should handle Snowflake databases with PRIORITY_DATABASE
        assertEquals(DatabaseObjectComparator.PRIORITY_DATABASE, 
                    comparator.getPriority(liquibase.database.object.Database.class, snowflakeDatabase));
        
        // Should not handle non-Snowflake databases
        assertEquals(DatabaseObjectComparator.PRIORITY_NONE, 
                    comparator.getPriority(liquibase.database.object.Database.class, postgresDatabase));
        
        // Should not handle non-Database objects
        assertEquals(DatabaseObjectComparator.PRIORITY_NONE, 
                    comparator.getPriority(Schema.class, snowflakeDatabase));
    }
    
    // ===== HASH GENERATION TESTS =====
    
    @Test
    void testHashGenerationWithValidDatabase() {
        liquibase.database.object.Database database = new liquibase.database.object.Database();
        database.setName("TEST_DB");
        
        String[] hash = comparator.hash(database, snowflakeDatabase, chain);
        
        assertNotNull(hash);
        assertEquals(1, hash.length);
        assertEquals("TEST_DB", hash[0]);
    }
    
    @Test
    void testHashGenerationWithNullName() {
        liquibase.database.object.Database database = new liquibase.database.object.Database();
        // Name is null by default
        
        String[] hash = comparator.hash(database, snowflakeDatabase, chain);
        
        assertNotNull(hash);
        assertEquals(1, hash.length);
        assertNull(hash[0]);
    }
    
    // ===== SAME OBJECT COMPARISON TESTS =====
    
    @Test
    void testIsSameObjectWithIdenticalNames() {
        liquibase.database.object.Database db1 = new liquibase.database.object.Database();
        db1.setName("TEST_DB");
        
        liquibase.database.object.Database db2 = new liquibase.database.object.Database();
        db2.setName("TEST_DB");
        
        assertTrue(comparator.isSameObject(db1, db2, snowflakeDatabase, chain));
    }
    
    @Test
    void testIsSameObjectWithCaseInsensitiveNames() {
        liquibase.database.object.Database db1 = new liquibase.database.object.Database();
        db1.setName("test_db");
        
        liquibase.database.object.Database db2 = new liquibase.database.object.Database();
        db2.setName("TEST_DB");
        
        assertTrue(comparator.isSameObject(db1, db2, snowflakeDatabase, chain));
    }
    
    @Test
    void testIsSameObjectWithDifferentNames() {
        liquibase.database.object.Database db1 = new liquibase.database.object.Database();
        db1.setName("DB1");
        
        liquibase.database.object.Database db2 = new liquibase.database.object.Database();
        db2.setName("DB2");
        
        assertFalse(comparator.isSameObject(db1, db2, snowflakeDatabase, chain));
    }
    
    @Test
    void testIsSameObjectWithBothNullNames() {
        liquibase.database.object.Database db1 = new liquibase.database.object.Database();
        // Names are null by default
        
        liquibase.database.object.Database db2 = new liquibase.database.object.Database();
        
        assertTrue(comparator.isSameObject(db1, db2, snowflakeDatabase, chain));
    }
    
    @Test
    void testIsSameObjectWithOneNullName() {
        liquibase.database.object.Database db1 = new liquibase.database.object.Database();
        db1.setName("TEST_DB");
        
        liquibase.database.object.Database db2 = new liquibase.database.object.Database();
        // Name is null by default
        
        assertFalse(comparator.isSameObject(db1, db2, snowflakeDatabase, chain));
        assertFalse(comparator.isSameObject(db2, db1, snowflakeDatabase, chain));
    }
    
    @Test
    void testIsSameObjectWithNonDatabaseObjects() {
        liquibase.database.object.Database database = new liquibase.database.object.Database();
        database.setName("TEST_DB");
        
        Schema schema = new Schema();
        
        assertFalse(comparator.isSameObject(database, schema, snowflakeDatabase, chain));
        assertFalse(comparator.isSameObject(schema, database, snowflakeDatabase, chain));
    }
    
    // ===== FIND DIFFERENCES TESTS =====
    
    @Test
    void testFindDifferencesWithIdenticalDatabases() {
        liquibase.database.object.Database db1 = createTestDatabase();
        liquibase.database.object.Database db2 = createTestDatabase();
        
        ObjectDifferences differences = comparator.findDifferences(
            db1, db2, snowflakeDatabase, compareControl, chain, new HashSet<>());
        
        assertNotNull(differences);
        assertFalse(differences.hasDifferences());
    }
    
    @Test
    void testFindDifferencesWithDifferentComments() {
        liquibase.database.object.Database db1 = createTestDatabase();
        db1.setComment("Original comment");
        
        liquibase.database.object.Database db2 = createTestDatabase();
        db2.setComment("Modified comment");
        
        ObjectDifferences differences = comparator.findDifferences(
            db1, db2, snowflakeDatabase, compareControl, chain, new HashSet<>());
        
        assertNotNull(differences);
        assertTrue(differences.hasDifferences());
    }
    
    @Test
    void testFindDifferencesWithDifferentRetentionTime() {
        liquibase.database.object.Database db1 = createTestDatabase();
        db1.setDataRetentionTimeInDays(7);
        
        liquibase.database.object.Database db2 = createTestDatabase();
        db2.setDataRetentionTimeInDays(14);
        
        ObjectDifferences differences = comparator.findDifferences(
            db1, db2, snowflakeDatabase, compareControl, chain, new HashSet<>());
        
        assertNotNull(differences);
        assertTrue(differences.hasDifferences());
    }
    
    @Test
    void testFindDifferencesWithDifferentTransientFlag() {
        liquibase.database.object.Database db1 = createTestDatabase();
        db1.setTransient(false);
        
        liquibase.database.object.Database db2 = createTestDatabase();
        db2.setTransient(true);
        
        ObjectDifferences differences = comparator.findDifferences(
            db1, db2, snowflakeDatabase, compareControl, chain, new HashSet<>());
        
        assertNotNull(differences);
        assertTrue(differences.hasDifferences());
    }
    
    @Test
    void testFindDifferencesWithDifferentCollation() {
        liquibase.database.object.Database db1 = createTestDatabase();
        db1.setDefaultDdlCollation("utf8");
        
        liquibase.database.object.Database db2 = createTestDatabase();
        db2.setDefaultDdlCollation("latin1");
        
        ObjectDifferences differences = comparator.findDifferences(
            db1, db2, snowflakeDatabase, compareControl, chain, new HashSet<>());
        
        assertNotNull(differences);
        assertTrue(differences.hasDifferences());
    }
    
    @Test
    void testFindDifferencesWithDifferentTags() {
        liquibase.database.object.Database db1 = createTestDatabase();
        db1.setTag("environment=dev");
        
        liquibase.database.object.Database db2 = createTestDatabase();
        db2.setTag("environment=prod");
        
        ObjectDifferences differences = comparator.findDifferences(
            db1, db2, snowflakeDatabase, compareControl, chain, new HashSet<>());
        
        assertNotNull(differences);
        assertTrue(differences.hasDifferences());
    }
    
    @Test
    void testFindDifferencesWithNullValues() {
        liquibase.database.object.Database db1 = createTestDatabase();
        db1.setComment(null);
        db1.setDataRetentionTimeInDays(null);
        
        liquibase.database.object.Database db2 = createTestDatabase();
        db2.setComment("Some comment");
        db2.setDataRetentionTimeInDays(7);
        
        ObjectDifferences differences = comparator.findDifferences(
            db1, db2, snowflakeDatabase, compareControl, chain, new HashSet<>());
        
        assertNotNull(differences);
        assertTrue(differences.hasDifferences());
    }
    
    @Test
    void testFindDifferencesExcludesStateProperties() {
        // State properties should be excluded from comparison
        liquibase.database.object.Database db1 = createTestDatabase();
        db1.setOwner("ADMIN1");
        db1.setDatabaseType("STANDARD");
        db1.setCreated(new java.util.Date(1000000L));
        
        liquibase.database.object.Database db2 = createTestDatabase();
        db2.setOwner("ADMIN2");
        db2.setDatabaseType("SHARED");
        db2.setCreated(new java.util.Date(2000000L));
        
        ObjectDifferences differences = comparator.findDifferences(
            db1, db2, snowflakeDatabase, compareControl, chain, new HashSet<>());
        
        assertNotNull(differences);
        // Should be equal because state properties are excluded
        assertFalse(differences.hasDifferences());
    }
    
    @Test
    void testFindDifferencesWithMultipleDifferences() {
        liquibase.database.object.Database db1 = createTestDatabase();
        db1.setComment("Comment 1");
        db1.setDataRetentionTimeInDays(7);
        db1.setTransient(false);
        
        liquibase.database.object.Database db2 = createTestDatabase();
        db2.setComment("Comment 2");
        db2.setDataRetentionTimeInDays(14);
        db2.setTransient(true);
        
        ObjectDifferences differences = comparator.findDifferences(
            db1, db2, snowflakeDatabase, compareControl, chain, new HashSet<>());
        
        assertNotNull(differences);
        assertTrue(differences.hasDifferences());
    }
    
    @Test
    void testFindDifferencesWithMaxDataExtensionTime() {
        liquibase.database.object.Database db1 = createTestDatabase();
        db1.setMaxDataExtensionTimeInDays(14);
        
        liquibase.database.object.Database db2 = createTestDatabase();
        db2.setMaxDataExtensionTimeInDays(30);
        
        ObjectDifferences differences = comparator.findDifferences(
            db1, db2, snowflakeDatabase, compareControl, chain, new HashSet<>());
        
        assertNotNull(differences);
        assertTrue(differences.hasDifferences());
    }
    
    // ===== HELPER METHODS =====
    
    private liquibase.database.object.Database createTestDatabase() {
        liquibase.database.object.Database database = new liquibase.database.object.Database();
        database.setName("TEST_DB");
        return database;
    }
    
    // ===== EDGE CASE TESTS =====
    
    @Test
    void testCompareFieldWithBothNull() {
        liquibase.database.object.Database db1 = createTestDatabase();
        liquibase.database.object.Database db2 = createTestDatabase();
        // Both have null comments by default
        
        ObjectDifferences differences = comparator.findDifferences(
            db1, db2, snowflakeDatabase, compareControl, chain, new HashSet<>());
        
        assertNotNull(differences);
        assertFalse(differences.hasDifferences());
    }
    
    @Test
    void testCompareFieldWithEmptyStringVsNull() {
        liquibase.database.object.Database db1 = createTestDatabase();
        db1.setComment("");
        
        liquibase.database.object.Database db2 = createTestDatabase();
        db2.setComment(null);
        
        ObjectDifferences differences = comparator.findDifferences(
            db1, db2, snowflakeDatabase, compareControl, chain, new HashSet<>());
        
        assertNotNull(differences);
        // Empty string should be treated as null by Database.setComment()
        assertFalse(differences.hasDifferences());
    }
}