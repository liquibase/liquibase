package liquibase.statement.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AlterTableStatement
 */
@DisplayName("AlterTableStatement")
public class AlterTableStatementTest {
    
    @Test
    @DisplayName("Should initialize with provided constructor values")
    public void testConstructorInitialization() {
        AlterTableStatement statement = new AlterTableStatement("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        
        assertEquals("TEST_CATALOG", statement.getCatalogName());
        assertEquals("TEST_SCHEMA", statement.getSchemaName());
        assertEquals("TEST_TABLE", statement.getTableName());
        
        // All other properties should be null initially
        assertNull(statement.getClusterBy());
        assertNull(statement.getDropClusteringKey());
        assertNull(statement.getSuspendRecluster());
        assertNull(statement.getResumeRecluster());
        assertNull(statement.getSetDataRetentionTimeInDays());
        assertNull(statement.getSetChangeTracking());
        assertNull(statement.getSetEnableSchemaEvolution());
    }
    
    @Test
    @DisplayName("Should handle null constructor values")
    public void testConstructorWithNulls() {
        AlterTableStatement statement = new AlterTableStatement(null, null, "TEST_TABLE");
        
        assertNull(statement.getCatalogName());
        assertNull(statement.getSchemaName());
        assertEquals("TEST_TABLE", statement.getTableName());
    }
    
    @Test
    @DisplayName("Should set and get catalog/schema/table names correctly")
    public void testBasicTableIdentifiers() {
        AlterTableStatement statement = new AlterTableStatement("OLD_CAT", "OLD_SCHEMA", "OLD_TABLE");
        
        // Test setters
        statement.setCatalogName("NEW_CATALOG");
        statement.setSchemaName("NEW_SCHEMA");
        statement.setTableName("NEW_TABLE");
        
        assertEquals("NEW_CATALOG", statement.getCatalogName());
        assertEquals("NEW_SCHEMA", statement.getSchemaName());
        assertEquals("NEW_TABLE", statement.getTableName());
    }
    
    @Test
    @DisplayName("Should set and get all clustering properties correctly")
    public void testClusteringProperties() {
        AlterTableStatement statement = new AlterTableStatement(null, null, "TEST_TABLE");
        
        // Test clusterBy
        assertNull(statement.getClusterBy());
        statement.setClusterBy("col1,col2,col3");
        assertEquals("col1,col2,col3", statement.getClusterBy());
        
        // Test dropClusteringKey
        assertNull(statement.getDropClusteringKey());
        statement.setDropClusteringKey(true);
        assertTrue(statement.getDropClusteringKey());
        
        // Test suspendRecluster
        assertNull(statement.getSuspendRecluster());
        statement.setSuspendRecluster(true);
        assertTrue(statement.getSuspendRecluster());
        
        // Test resumeRecluster
        assertNull(statement.getResumeRecluster());
        statement.setResumeRecluster(true);
        assertTrue(statement.getResumeRecluster());
    }
    
    @Test
    @DisplayName("Should set and get all property settings correctly")
    public void testPropertySettings() {
        AlterTableStatement statement = new AlterTableStatement(null, null, "TEST_TABLE");
        
        // Test setDataRetentionTimeInDays
        assertNull(statement.getSetDataRetentionTimeInDays());
        statement.setSetDataRetentionTimeInDays(30);
        assertEquals(30, statement.getSetDataRetentionTimeInDays().intValue());
        
        // Test setChangeTracking
        assertNull(statement.getSetChangeTracking());
        statement.setSetChangeTracking(true);
        assertTrue(statement.getSetChangeTracking());
        statement.setSetChangeTracking(false);
        assertFalse(statement.getSetChangeTracking());
        
        // Test setEnableSchemaEvolution
        assertNull(statement.getSetEnableSchemaEvolution());
        statement.setSetEnableSchemaEvolution(true);
        assertTrue(statement.getSetEnableSchemaEvolution());
        statement.setSetEnableSchemaEvolution(false);
        assertFalse(statement.getSetEnableSchemaEvolution());
    }
    
    @Test
    @DisplayName("Should handle null values properly")
    public void testNullHandling() {
        AlterTableStatement statement = new AlterTableStatement(null, null, "TEST_TABLE");
        
        // Set values then set back to null
        statement.setClusterBy("col1,col2");
        statement.setClusterBy(null);
        assertNull(statement.getClusterBy());
        
        statement.setDropClusteringKey(true);
        statement.setDropClusteringKey(null);
        assertNull(statement.getDropClusteringKey());
        
        statement.setSuspendRecluster(true);
        statement.setSuspendRecluster(null);
        assertNull(statement.getSuspendRecluster());
        
        statement.setResumeRecluster(true);
        statement.setResumeRecluster(null);
        assertNull(statement.getResumeRecluster());
        
        statement.setSetDataRetentionTimeInDays(30);
        statement.setSetDataRetentionTimeInDays(null);
        assertNull(statement.getSetDataRetentionTimeInDays());
        
        statement.setSetChangeTracking(true);
        statement.setSetChangeTracking(null);
        assertNull(statement.getSetChangeTracking());
        
        statement.setSetEnableSchemaEvolution(true);
        statement.setSetEnableSchemaEvolution(null);
        assertNull(statement.getSetEnableSchemaEvolution());
    }
    
    @Test
    @DisplayName("Should support edge case values")
    public void testEdgeCaseValues() {
        AlterTableStatement statement = new AlterTableStatement(null, null, "TEST_TABLE");
        
        // Test empty strings
        statement.setClusterBy("");
        assertEquals("", statement.getClusterBy());
        
        // Test boundary values for retention time
        statement.setSetDataRetentionTimeInDays(0);
        assertEquals(0, statement.getSetDataRetentionTimeInDays().intValue());
        
        statement.setSetDataRetentionTimeInDays(90);
        assertEquals(90, statement.getSetDataRetentionTimeInDays().intValue());
        
        // Test boolean false explicitly
        statement.setDropClusteringKey(false);
        assertFalse(statement.getDropClusteringKey());
        
        statement.setSuspendRecluster(false);
        assertFalse(statement.getSuspendRecluster());
        
        statement.setResumeRecluster(false);
        assertFalse(statement.getResumeRecluster());
        
        statement.setSetChangeTracking(false);
        assertFalse(statement.getSetChangeTracking());
        
        statement.setSetEnableSchemaEvolution(false);
        assertFalse(statement.getSetEnableSchemaEvolution());
    }
    
    @Test
    @DisplayName("Should handle complex clustering expressions")
    public void testComplexClusteringExpressions() {
        AlterTableStatement statement = new AlterTableStatement(null, null, "TEST_TABLE");
        
        // Test complex expressions
        statement.setClusterBy("SUBSTR(name, 1, 3), created_date, id");
        assertEquals("SUBSTR(name, 1, 3), created_date, id", statement.getClusterBy());
        
        // Test with special characters
        statement.setClusterBy("\"COLUMN-WITH-DASHES\", normal_column");
        assertEquals("\"COLUMN-WITH-DASHES\", normal_column", statement.getClusterBy());
        
        // Test single column
        statement.setClusterBy("single_column");
        assertEquals("single_column", statement.getClusterBy());
    }
}