package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.AlterTableChange;
import liquibase.change.core.SetTableRemarksChange;
import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.diff.Difference;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.structure.DatabaseObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/**
 * Comprehensive unit tests for ChangedTableChangeGeneratorSnowflake with 90%+ coverage focus.
 * Tests all diff handling methods, boolean conversion, Snowflake-specific table property changes.
 * Follows established testing patterns: diff processing, change generation, complete validation.
 */
@DisplayName("ChangedTableChangeGeneratorSnowflake")
public class ChangedTableChangeGeneratorSnowflakeTest {

    private ChangedTableChangeGeneratorSnowflake generator;
    private SnowflakeDatabase database;
    private Table table;
    private Schema schema;
    private DiffOutputControl control;
    private ChangeGeneratorChain chain;
    private ObjectDifferences differences;
    private CompareControl compareControl;

    @BeforeEach
    void setUp() {
        generator = new ChangedTableChangeGeneratorSnowflake();
        database = new SnowflakeDatabase();
        
        // Setup table with schema and catalog
        Catalog catalog = new Catalog("TEST_CATALOG");
        schema = new Schema(catalog, "TEST_SCHEMA");
        table = new Table();
        table.setName("TEST_TABLE");
        table.setSchema(schema);
        
        // Setup control and chain
        control = mock(DiffOutputControl.class);
        chain = mock(ChangeGeneratorChain.class);
        compareControl = mock(CompareControl.class);
        
        // Mock chain to return null for parent class delegations (standard behavior for comments)
        when(chain.fixChanged(any(DatabaseObject.class), any(ObjectDifferences.class), any(DiffOutputControl.class), any(Database.class), any(Database.class))).thenReturn(null);
        
        // Setup differences
        differences = new ObjectDifferences(compareControl);
    }

    // ==================== Priority Tests ====================
    
    @Test
    @DisplayName("Should have high priority for Snowflake Table objects")
    void shouldHaveHighPriorityForSnowflakeTableObjects() {
        int priority = generator.getPriority(Table.class, database);
        
        assertTrue(priority > ChangedTableChangeGeneratorSnowflake.PRIORITY_NONE);
        // Priority calculation is based on parent implementation + database priority
    }
    
    @Test
    @DisplayName("Should have no priority for non-Snowflake databases")
    void shouldHaveNoPriorityForNonSnowflakeDatabases() {
        H2Database h2Database = new H2Database();
        
        int priority = generator.getPriority(Table.class, h2Database);
        
        assertEquals(ChangedTableChangeGeneratorSnowflake.PRIORITY_NONE, priority);
    }
    
    @Test
    @DisplayName("Should have no priority for non-Table objects")
    void shouldHaveNoPriorityForNonTableObjects() {
        int priority = generator.getPriority(Schema.class, database);
        
        assertEquals(ChangedTableChangeGeneratorSnowflake.PRIORITY_NONE, priority);
    }
    
    @Test
    @DisplayName("Should handle null database gracefully")
    void shouldHandleNullDatabaseGracefully() {
        int priority = generator.getPriority(Table.class, null);
        
        assertEquals(ChangedTableChangeGeneratorSnowflake.PRIORITY_NONE, priority);
    }

    // ==================== Clustering Key Changes ====================
    
    @Test
    @DisplayName("Should generate ALTER TABLE with new clustering key")
    void shouldGenerateAlterTableWithNewClusteringKey() {
        // Setup clustering key difference
        differences.addDifference("clusteringKey", null, "column1, column2");
        
        when(control.getIncludeCatalog()).thenReturn(true);
        when(control.getIncludeSchema()).thenReturn(true);
        
        Change[] changes = generator.fixChanged(table, differences, control, database, database, chain);
        
        assertNotNull(changes);
        assertEquals(1, changes.length);
        assertTrue(changes[0] instanceof AlterTableChange);
        
        AlterTableChange alterTableChange = (AlterTableChange) changes[0];
        assertEquals("TEST_CATALOG", alterTableChange.getCatalogName());
        assertEquals("TEST_SCHEMA", alterTableChange.getSchemaName());
        assertEquals("TEST_TABLE", alterTableChange.getTableName());
        assertEquals("column1, column2", alterTableChange.getClusterBy());
    }
    
    @Test
    @DisplayName("Should generate ALTER TABLE to drop clustering key")
    void shouldGenerateAlterTableToDropClusteringKey() {
        // Setup clustering key difference (removing existing key)
        differences.addDifference("clusteringKey", "old_key", null);
        
        when(control.getIncludeCatalog()).thenReturn(true);
        when(control.getIncludeSchema()).thenReturn(true);
        
        Change[] changes = generator.fixChanged(table, differences, control, database, database, chain);
        
        assertNotNull(changes);
        assertEquals(1, changes.length);
        assertTrue(changes[0] instanceof AlterTableChange);
        
        AlterTableChange alterTableChange = (AlterTableChange) changes[0];
        assertEquals("TEST_CATALOG", alterTableChange.getCatalogName());
        assertEquals("TEST_SCHEMA", alterTableChange.getSchemaName());
        assertEquals("TEST_TABLE", alterTableChange.getTableName());
        assertEquals(Boolean.TRUE, alterTableChange.getDropClusteringKey());
    }
    
    @Test
    @DisplayName("Should handle clustering key change from old to new")
    void shouldHandleClusteringKeyChangeFromOldToNew() {
        // Setup clustering key difference (changing from old to new key)
        differences.addDifference("clusteringKey", "old_key", "new_key");
        
        when(control.getIncludeCatalog()).thenReturn(false);
        when(control.getIncludeSchema()).thenReturn(false);
        
        Change[] changes = generator.fixChanged(table, differences, control, database, database, chain);
        
        assertNotNull(changes);
        assertEquals(1, changes.length);
        assertTrue(changes[0] instanceof AlterTableChange);
        
        AlterTableChange alterTableChange = (AlterTableChange) changes[0];
        assertNull(alterTableChange.getCatalogName()); // Include catalog = false
        assertNull(alterTableChange.getSchemaName());  // Include schema = false
        assertEquals("TEST_TABLE", alterTableChange.getTableName());
        assertEquals("new_key", alterTableChange.getClusterBy());
    }

    // ==================== Data Retention Time Changes ====================
    
    @Test
    @DisplayName("Should generate ALTER TABLE with data retention time")
    void shouldGenerateAlterTableWithDataRetentionTime() {
        differences.addDifference("retentionTime", null, "30");
        
        when(control.getIncludeCatalog()).thenReturn(true);
        when(control.getIncludeSchema()).thenReturn(true);
        
        Change[] changes = generator.fixChanged(table, differences, control, database, database, chain);
        
        assertNotNull(changes);
        assertEquals(1, changes.length);
        assertTrue(changes[0] instanceof AlterTableChange);
        
        AlterTableChange alterTableChange = (AlterTableChange) changes[0];
        assertEquals(Integer.valueOf(30), alterTableChange.getSetDataRetentionTimeInDays());
    }
    
    @Test
    @DisplayName("Should handle invalid retention time gracefully")
    void shouldHandleInvalidRetentionTimeGracefully() {
        differences.addDifference("retentionTime", null, "invalid_number");
        
        when(control.getIncludeCatalog()).thenReturn(true);
        when(control.getIncludeSchema()).thenReturn(true);
        
        Change[] changes = generator.fixChanged(table, differences, control, database, database, chain);
        
        // Should not generate changes for invalid number format
        assertNull(changes);
    }
    
    @Test
    @DisplayName("Should ignore null retention time")
    void shouldIgnoreNullRetentionTime() {
        differences.addDifference("retentionTime", null, "null");
        
        Change[] changes = generator.fixChanged(table, differences, control, database, database, chain);
        
        assertNull(changes);
    }
    
    @Test
    @DisplayName("Should ignore empty retention time")
    void shouldIgnoreEmptyRetentionTime() {
        differences.addDifference("retentionTime", null, "");
        
        Change[] changes = generator.fixChanged(table, differences, control, database, database, chain);
        
        assertNull(changes);
    }

    // ==================== Change Tracking Changes ====================
    
    @Test
    @DisplayName("Should generate ALTER TABLE with change tracking enabled")
    void shouldGenerateAlterTableWithChangeTrackingEnabled() {
        differences.addDifference("changeTracking", "false", "true");
        
        when(control.getIncludeCatalog()).thenReturn(true);
        when(control.getIncludeSchema()).thenReturn(true);
        
        Change[] changes = generator.fixChanged(table, differences, control, database, database, chain);
        
        assertNotNull(changes);
        assertEquals(1, changes.length);
        assertTrue(changes[0] instanceof AlterTableChange);
        
        AlterTableChange alterTableChange = (AlterTableChange) changes[0];
        assertEquals(Boolean.TRUE, alterTableChange.getSetChangeTracking());
    }
    
    @Test
    @DisplayName("Should generate ALTER TABLE with change tracking disabled")
    void shouldGenerateAlterTableWithChangeTrackingDisabled() {
        differences.addDifference("changeTracking", "true", "false");
        
        when(control.getIncludeCatalog()).thenReturn(true);
        when(control.getIncludeSchema()).thenReturn(true);
        
        Change[] changes = generator.fixChanged(table, differences, control, database, database, chain);
        
        assertNotNull(changes);
        assertEquals(1, changes.length);
        assertTrue(changes[0] instanceof AlterTableChange);
        
        AlterTableChange alterTableChange = (AlterTableChange) changes[0];
        assertEquals(Boolean.FALSE, alterTableChange.getSetChangeTracking());
    }

    // ==================== Schema Evolution Changes ====================
    
    @Test
    @DisplayName("Should generate ALTER TABLE with schema evolution enabled")
    void shouldGenerateAlterTableWithSchemaEvolutionEnabled() {
        differences.addDifference("enableSchemaEvolution", "false", "true");
        
        when(control.getIncludeCatalog()).thenReturn(true);
        when(control.getIncludeSchema()).thenReturn(true);
        
        Change[] changes = generator.fixChanged(table, differences, control, database, database, chain);
        
        assertNotNull(changes);
        assertEquals(1, changes.length);
        assertTrue(changes[0] instanceof AlterTableChange);
        
        AlterTableChange alterTableChange = (AlterTableChange) changes[0];
        assertEquals(Boolean.TRUE, alterTableChange.getSetEnableSchemaEvolution());
    }
    
    @Test
    @DisplayName("Should generate ALTER TABLE with schema evolution disabled")
    void shouldGenerateAlterTableWithSchemaEvolutionDisabled() {
        differences.addDifference("enableSchemaEvolution", "true", "false");
        
        when(control.getIncludeCatalog()).thenReturn(true);
        when(control.getIncludeSchema()).thenReturn(true);
        
        Change[] changes = generator.fixChanged(table, differences, control, database, database, chain);
        
        assertNotNull(changes);
        assertEquals(1, changes.length);
        assertTrue(changes[0] instanceof AlterTableChange);
        
        AlterTableChange alterTableChange = (AlterTableChange) changes[0];
        assertEquals(Boolean.FALSE, alterTableChange.getSetEnableSchemaEvolution());
    }

    // ==================== Boolean Conversion Tests ====================
    
    @Test
    @DisplayName("Should convert various true values correctly")
    void shouldConvertVariousTrueValuesCorrectly() {
        // Test various boolean formats that should convert to true
        String[] trueValues = {"yes", "YES", "y", "Y", "on", "ON", "true", "TRUE", "1"};
        
        for (String trueValue : trueValues) {
            differences = new ObjectDifferences(compareControl);
            differences.addDifference("changeTracking", "false", trueValue);
            
            when(control.getIncludeCatalog()).thenReturn(false);
            when(control.getIncludeSchema()).thenReturn(false);
            
            Change[] changes = generator.fixChanged(table, differences, control, database, database, chain);
            
            assertNotNull(changes, "Should generate change for true value: " + trueValue);
            assertEquals(1, changes.length);
            AlterTableChange alterTableChange = (AlterTableChange) changes[0];
            assertEquals(Boolean.TRUE, alterTableChange.getSetChangeTracking(), 
                         "Should set change tracking to TRUE for value: " + trueValue);
        }
    }
    
    @Test
    @DisplayName("Should convert various false values correctly")
    void shouldConvertVariousFalseValuesCorrectly() {
        // Test various boolean formats that should convert to false
        String[] falseValues = {"no", "NO", "n", "N", "off", "OFF", "false", "FALSE", "0"};
        
        for (String falseValue : falseValues) {
            differences = new ObjectDifferences(compareControl);
            differences.addDifference("changeTracking", "true", falseValue);
            
            when(control.getIncludeCatalog()).thenReturn(false);
            when(control.getIncludeSchema()).thenReturn(false);
            
            Change[] changes = generator.fixChanged(table, differences, control, database, database, chain);
            
            assertNotNull(changes, "Should generate change for false value: " + falseValue);
            assertEquals(1, changes.length);
            AlterTableChange alterTableChange = (AlterTableChange) changes[0];
            assertEquals(Boolean.FALSE, alterTableChange.getSetChangeTracking(), 
                         "Should set change tracking to FALSE for value: " + falseValue);
        }
    }
    
    @Test
    @DisplayName("Should handle invalid boolean values gracefully")
    void shouldHandleInvalidBooleanValuesGracefully() {
        String[] invalidValues = {"invalid", "maybe", "null", "", "   "};
        
        for (String invalidValue : invalidValues) {
            differences = new ObjectDifferences(compareControl);
            differences.addDifference("changeTracking", "true", invalidValue);
            
            Change[] changes = generator.fixChanged(table, differences, control, database, database, chain);
            
            // Should not generate changes for invalid boolean values
            assertNull(changes, "Should not generate change for invalid boolean: " + invalidValue);
        }
    }

    // ==================== Multiple Changes Tests ====================
    
    @Test
    @DisplayName("Should generate multiple changes for multiple differences")
    void shouldGenerateMultipleChangesForMultipleDifferences() {
        // Setup multiple differences
        differences.addDifference("clusteringKey", null, "col1");
        differences.addDifference("retentionTime", null, "15");
        differences.addDifference("changeTracking", "false", "true");
        
        when(control.getIncludeCatalog()).thenReturn(true);
        when(control.getIncludeSchema()).thenReturn(true);
        
        Change[] changes = generator.fixChanged(table, differences, control, database, database, chain);
        
        assertNotNull(changes);
        assertEquals(3, changes.length);
        
        // Verify all changes are AlterTableChange
        for (Change change : changes) {
            assertTrue(change instanceof AlterTableChange);
            AlterTableChange alterChange = (AlterTableChange) change;
            assertEquals("TEST_CATALOG", alterChange.getCatalogName());
            assertEquals("TEST_SCHEMA", alterChange.getSchemaName());
            assertEquals("TEST_TABLE", alterChange.getTableName());
        }
        
        // Verify specific properties were set (order may vary)
        boolean foundClustering = false, foundRetention = false, foundChangeTracking = false;
        for (Change change : changes) {
            AlterTableChange alterChange = (AlterTableChange) change;
            if ("col1".equals(alterChange.getClusterBy())) {
                foundClustering = true;
            }
            if (Integer.valueOf(15).equals(alterChange.getSetDataRetentionTimeInDays())) {
                foundRetention = true;
            }
            if (Boolean.TRUE.equals(alterChange.getSetChangeTracking())) {
                foundChangeTracking = true;
            }
        }
        assertTrue(foundClustering, "Should have clustering key change");
        assertTrue(foundRetention, "Should have retention time change");
        assertTrue(foundChangeTracking, "Should have change tracking change");
    }
    
    @Test
    @DisplayName("Should handle no differences gracefully")
    void shouldHandleNoDifferencesGracefully() {
        // Empty differences object
        ObjectDifferences emptyDifferences = new ObjectDifferences(compareControl);
        
        Change[] changes = generator.fixChanged(table, emptyDifferences, control, database, database, chain);
        
        assertNull(changes);
    }

    // ==================== Edge Cases and Error Handling ====================
    
    @Test
    @DisplayName("Should handle null table gracefully")
    void shouldHandleNullTableGracefully() {
        differences.addDifference("clusteringKey", null, "col1");
        
        // This should throw a ClassCastException or NullPointerException - testing error handling
        assertThrows(Exception.class, () -> {
            generator.fixChanged(null, differences, control, database, database, chain);
        });
    }
    
    @Test
    @DisplayName("Should handle null differences gracefully")
    void shouldHandleNullDifferencesGracefully() {
        Change[] changes = generator.fixChanged(table, null, control, database, database, chain);
        
        // Should handle null differences without crashing
        assertNull(changes);
    }
    
    @Test
    @DisplayName("Should handle table without schema gracefully")
    void shouldHandleTableWithoutSchemaGracefully() {
        Table tableWithoutSchema = new Table();
        tableWithoutSchema.setName("TABLE_NO_SCHEMA");
        // No schema set
        
        differences.addDifference("clusteringKey", null, "col1");
        
        when(control.getIncludeCatalog()).thenReturn(true);
        when(control.getIncludeSchema()).thenReturn(true);
        
        // Should handle missing schema gracefully
        assertThrows(Exception.class, () -> {
            generator.fixChanged(tableWithoutSchema, differences, control, database, database, chain);
        });
    }

    // ==================== Comment Handling Tests ====================
    
    @Test
    @DisplayName("Should handle comment changes with standard handling")
    void shouldHandleCommentChangesWithStandardHandling() {
        // Current implementation returns false for isSnowflakeSpecificCommentHandlingNeeded
        // So standard parent class handling should be used
        differences.addDifference("remarks", "old comment", "new comment");
        
        when(control.getIncludeCatalog()).thenReturn(true);
        when(control.getIncludeSchema()).thenReturn(true);
        
        Change[] changes = generator.fixChanged(table, differences, control, database, database, chain);
        
        // Should use parent class handling (mocked chain would return null)
        assertNull(changes);
    }
    
    @Test
    @DisplayName("Should use standard comment handling by default")
    void shouldUseStandardCommentHandlingByDefault() {
        // The isSnowflakeSpecificCommentHandlingNeeded method currently returns false
        // This test verifies that behavior
        differences.addDifference("remarks", null, "test comment");
        
        Change[] changes = generator.fixChanged(table, differences, control, database, database, chain);
        
        // With mocked chain returning null for parent changes, should get null result
        assertNull(changes);
    }
}