package liquibase.diff.output;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.diff.compare.DatabaseObjectComparatorChain;
import liquibase.structure.core.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Table comparator for Snowflake.
 * Tests diff functionality for Snowflake Table objects with Snowflake-specific attributes.
 * 
 * ADDRESSES_CORE_ISSUE: Complete TDD coverage for Table object comparison and diff generation.
 */
public class TableComparatorSnowflakeTest {

    @Mock
    private Database database;
    
    @Mock
    private SnowflakeDatabase snowflakeDatabase;
    
    @Mock
    private DatabaseObjectComparatorChain chain;
    
    private TableComparatorSnowflake comparator;
    private CompareControl compareControl;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        comparator = new TableComparatorSnowflake();
        compareControl = new CompareControl();
    }

    @Test
    public void testGetPriorityForTableWithSnowflakeDatabase() {
        int priority = comparator.getPriority(Table.class, snowflakeDatabase);
        assertEquals(DatabaseObjectComparator.PRIORITY_DATABASE, priority);
    }

    @Test
    public void testGetPriorityForTableWithNonSnowflakeDatabase() {
        int priority = comparator.getPriority(Table.class, database);
        assertEquals(DatabaseObjectComparator.PRIORITY_NONE, priority);
    }

    @Test
    public void testGetPriorityForNonTableObject() {
        int priority = comparator.getPriority(liquibase.database.object.Schema.class, snowflakeDatabase);
        assertEquals(DatabaseObjectComparator.PRIORITY_NONE, priority);
    }

    @Test
    public void testIsSameObjectWithIdenticalTables() {
        Table table1 = createTestTable("SAME_TABLE", "PUBLIC");
        Table table2 = createTestTable("SAME_TABLE", "PUBLIC");
        
        boolean isSame = comparator.isSameObject(table1, table2, snowflakeDatabase, chain);
        
        assertTrue(isSame);
    }

    @Test
    public void testIsSameObjectWithDifferentTables() {
        Table table1 = createTestTable("TABLE_ONE", "PUBLIC");
        Table table2 = createTestTable("TABLE_TWO", "PUBLIC");
        
        boolean isSame = comparator.isSameObject(table1, table2, snowflakeDatabase, chain);
        
        assertFalse(isSame);
    }

    @Test
    public void testIsSameObjectWithDifferentSchemas() {
        Table table1 = createTestTable("SAME_TABLE", "SCHEMA_ONE");
        Table table2 = createTestTable("SAME_TABLE", "SCHEMA_TWO");
        
        boolean isSame = comparator.isSameObject(table1, table2, snowflakeDatabase, chain);
        
        assertFalse(isSame);
    }

    @Test
    public void testIsSameObjectWithNonTableObjects() {
        Table table = createTestTable("TABLE", "PUBLIC");
        liquibase.database.object.Schema schema = new liquibase.database.object.Schema("SCHEMA");
        
        boolean isSame = comparator.isSameObject(table, schema, snowflakeDatabase, chain);
        
        assertFalse(isSame);
    }

    @Test
    public void testFindDifferencesWithIdenticalTables() {
        Table table1 = createTestTable("IDENTICAL_TABLE", "PUBLIC");
        table1.setAttribute("clusteringKey", "ID, NAME");
        table1.setAttribute("retentionTime", "7");
        table1.setAttribute("isTransient", "NO");
        
        Table table2 = createTestTable("IDENTICAL_TABLE", "PUBLIC");
        table2.setAttribute("clusteringKey", "ID, NAME");
        table2.setAttribute("retentionTime", "7");
        table2.setAttribute("isTransient", "NO");
        
        ObjectDifferences differences = comparator.findDifferences(
            table1, table2, snowflakeDatabase, compareControl, chain, new HashSet<>()
        );
        
        assertFalse(differences.hasDifferences());
    }

    @Test
    public void testFindDifferencesWithDifferentClusteringKey() {
        Table table1 = createTestTable("CLUSTER_TABLE", "PUBLIC");
        table1.setAttribute("clusteringKey", "ID, NAME");
        table1.setAttribute("retentionTime", "7");
        
        Table table2 = createTestTable("CLUSTER_TABLE", "PUBLIC");
        table2.setAttribute("clusteringKey", "ID, DATE");
        table2.setAttribute("retentionTime", "7");
        
        ObjectDifferences differences = comparator.findDifferences(
            table1, table2, snowflakeDatabase, compareControl, chain, new HashSet<>()
        );
        
        assertTrue(differences.hasDifferences());
    }

    @Test
    public void testFindDifferencesWithDifferentRetentionTime() {
        Table table1 = createTestTable("RETENTION_TABLE", "PUBLIC");
        table1.setAttribute("clusteringKey", "ID");
        table1.setAttribute("retentionTime", "7");
        
        Table table2 = createTestTable("RETENTION_TABLE", "PUBLIC");
        table2.setAttribute("clusteringKey", "ID");
        table2.setAttribute("retentionTime", "14");
        
        ObjectDifferences differences = comparator.findDifferences(
            table1, table2, snowflakeDatabase, compareControl, chain, new HashSet<>()
        );
        
        assertTrue(differences.hasDifferences());
    }

    @Test
    public void testFindDifferencesWithDifferentTransientFlag() {
        Table table1 = createTestTable("TRANSIENT_TABLE", "PUBLIC");
        table1.setAttribute("isTransient", "NO");
        
        Table table2 = createTestTable("TRANSIENT_TABLE", "PUBLIC");
        table2.setAttribute("isTransient", "YES");
        
        ObjectDifferences differences = comparator.findDifferences(
            table1, table2, snowflakeDatabase, compareControl, chain, new HashSet<>()
        );
        
        assertTrue(differences.hasDifferences());
    }

    @Test
    public void testFindDifferencesIgnoresStateProperties() {
        Table table1 = createTestTable("STATE_TABLE", "PUBLIC");
        table1.setAttribute("clusteringKey", "ID");
        table1.setAttribute("retentionTime", "7");
        table1.setAttribute("created", "2024-01-01"); // State property
        table1.setAttribute("lastAltered", "2024-01-02"); // State property
        table1.setAttribute("owner", "OWNER1"); // State property
        table1.setAttribute("rowCount", "1000"); // State property
        table1.setAttribute("bytes", "5000"); // State property
        
        Table table2 = createTestTable("STATE_TABLE", "PUBLIC");
        table2.setAttribute("clusteringKey", "ID");
        table2.setAttribute("retentionTime", "7");
        table2.setAttribute("created", "2024-02-01"); // Different state property
        table2.setAttribute("lastAltered", "2024-02-02"); // Different state property
        table2.setAttribute("owner", "OWNER2"); // Different state property
        table2.setAttribute("rowCount", "2000"); // Different state property
        table2.setAttribute("bytes", "10000"); // Different state property
        
        ObjectDifferences differences = comparator.findDifferences(
            table1, table2, snowflakeDatabase, compareControl, chain, new HashSet<>()
        );
        
        // Should not have differences because only state properties are different
        assertFalse(differences.hasDifferences());
    }

    @Test
    public void testFindDifferencesWithNullValues() {
        Table table1 = createTestTable("NULL_TABLE", "PUBLIC");
        table1.setAttribute("clusteringKey", null);
        table1.setAttribute("retentionTime", null);
        
        Table table2 = createTestTable("NULL_TABLE", "PUBLIC");
        table2.setAttribute("clusteringKey", "ID");
        table2.setAttribute("retentionTime", "7");
        
        ObjectDifferences differences = comparator.findDifferences(
            table1, table2, snowflakeDatabase, compareControl, chain, new HashSet<>()
        );
        
        assertTrue(differences.hasDifferences());
    }

    @Test
    public void testFindDifferencesWithMixedConfigurationChanges() {
        Table table1 = createTestTable("MIXED_TABLE", "PUBLIC");
        table1.setAttribute("clusteringKey", "OLD_KEY");
        table1.setAttribute("retentionTime", "7");
        table1.setAttribute("isTransient", "NO");
        
        Table table2 = createTestTable("MIXED_TABLE", "PUBLIC");
        table2.setAttribute("clusteringKey", "NEW_KEY");
        table2.setAttribute("retentionTime", "14");
        table2.setAttribute("isTransient", "YES");
        
        ObjectDifferences differences = comparator.findDifferences(
            table1, table2, snowflakeDatabase, compareControl, chain, new HashSet<>()
        );
        
        assertTrue(differences.hasDifferences());
    }

    @Test
    public void testFindDifferencesWithIdenticalNullValues() {
        Table table1 = createTestTable("NULL_VALUES_TABLE", "PUBLIC");
        table1.setAttribute("clusteringKey", null);
        table1.setAttribute("retentionTime", null);
        table1.setAttribute("isTransient", null);
        
        Table table2 = createTestTable("NULL_VALUES_TABLE", "PUBLIC");
        table2.setAttribute("clusteringKey", null);
        table2.setAttribute("retentionTime", null);
        table2.setAttribute("isTransient", null);
        
        ObjectDifferences differences = comparator.findDifferences(
            table1, table2, snowflakeDatabase, compareControl, chain, new HashSet<>()
        );
        
        assertFalse(differences.hasDifferences());
    }

    @Test
    public void testFindDifferencesWithOnlyConfigurationProperties() {
        Table table1 = createTestTable("CONFIG_ONLY_TABLE", "PUBLIC");
        // Set only configuration properties
        table1.setAttribute("clusteringKey", "ID, DATE");
        table1.setAttribute("retentionTime", "30");
        table1.setAttribute("isTransient", "YES");
        
        Table table2 = createTestTable("CONFIG_ONLY_TABLE", "PUBLIC");
        // Same configuration properties
        table2.setAttribute("clusteringKey", "ID, DATE");
        table2.setAttribute("retentionTime", "30");
        table2.setAttribute("isTransient", "YES");
        
        ObjectDifferences differences = comparator.findDifferences(
            table1, table2, snowflakeDatabase, compareControl, chain, new HashSet<>()
        );
        
        assertFalse(differences.hasDifferences());
    }

    private Table createTestTable(String name, String schemaName) {
        Table table = new Table();
        table.setName(name);
        table.setSchema(new liquibase.structure.core.Schema((liquibase.structure.core.Catalog) null, schemaName));
        return table;
    }
}