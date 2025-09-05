package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.statement.core.AlterWarehouseStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure unit tests for AlterWarehouseGeneratorSnowflake SQL generation
 * Tests SQL string output without database dependencies - NO MOCKING!
 */
@DisplayName("AlterWarehouseGeneratorSnowflake - Pure SQL Tests")
public class AlterWarehouseGeneratorSnowflakeTest {
    
    private AlterWarehouseGeneratorSnowflake generator;
    private SnowflakeDatabase database;
    
    @BeforeEach
    void setUp() {
        generator = new AlterWarehouseGeneratorSnowflake();
        database = new SnowflakeDatabase();
    }
    
    @Test
    @DisplayName("Should generate basic ALTER WAREHOUSE SQL")
    void testBasicAlterWarehouse() {
        // Given
        AlterWarehouseStatement statement = new AlterWarehouseStatement();
        statement.setWarehouseName("TEST_WH");
        statement.setWarehouseSize("LARGE");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        assertEquals("ALTER WAREHOUSE TEST_WH SET WAREHOUSE_SIZE = LARGE", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate ALTER WAREHOUSE with IF EXISTS")
    void testAlterWarehouseIfExists() {
        // Given
        AlterWarehouseStatement statement = new AlterWarehouseStatement();
        statement.setWarehouseName("TEST_WH");
        statement.setIfExists(true);
        statement.setAutoSuspend(600);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        assertEquals("ALTER WAREHOUSE IF EXISTS TEST_WH SET AUTO_SUSPEND = 600", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate warehouse rename")
    void testRenameWarehouse() {
        // Given
        AlterWarehouseStatement statement = new AlterWarehouseStatement();
        statement.setWarehouseName("OLD_WH");
        statement.setNewName("NEW_WH");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        assertEquals("ALTER WAREHOUSE OLD_WH RENAME TO NEW_WH", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate warehouse suspension")
    void testSuspendWarehouse() {
        // Given
        AlterWarehouseStatement statement = new AlterWarehouseStatement();
        statement.setWarehouseName("TEST_WH");
        statement.setAction("SUSPEND");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        assertEquals("ALTER WAREHOUSE TEST_WH SUSPEND", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate warehouse resume")
    void testResumeWarehouse() {
        // Given
        AlterWarehouseStatement statement = new AlterWarehouseStatement();
        statement.setWarehouseName("TEST_WH");
        statement.setAction("RESUME");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        assertEquals("ALTER WAREHOUSE TEST_WH RESUME", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate warehouse abort all queries")
    void testAbortAllQueries() {
        // Given
        AlterWarehouseStatement statement = new AlterWarehouseStatement();
        statement.setWarehouseName("TEST_WH");
        statement.setAction("ABORT ALL QUERIES");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        assertEquals("ALTER WAREHOUSE TEST_WH ABORT ALL QUERIES", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate warehouse with multiple SET properties")
    void testMultipleSetProperties() {
        // Given
        AlterWarehouseStatement statement = new AlterWarehouseStatement();
        statement.setWarehouseName("TEST_WH");
        statement.setWarehouseSize("XLARGE");
        statement.setAutoSuspend(300);
        statement.setAutoResume(true);
        statement.setMinClusterCount(2);
        statement.setMaxClusterCount(8);
        statement.setScalingPolicy("ECONOMY");
        statement.setComment("Updated warehouse");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String expectedSQL = "ALTER WAREHOUSE TEST_WH SET " +
                "WAREHOUSE_SIZE = XLARGE, " +
                "MAX_CLUSTER_COUNT = 8, " +
                "MIN_CLUSTER_COUNT = 2, " +
                "SCALING_POLICY = ECONOMY, " +
                "AUTO_SUSPEND = 300, " +
                "AUTO_RESUME = true, " +
                "COMMENT = 'Updated warehouse'";
        assertEquals(expectedSQL, sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate warehouse with UNSET properties")
    void testUnsetProperties() {
        // Given
        AlterWarehouseStatement statement = new AlterWarehouseStatement();
        statement.setWarehouseName("TEST_WH");
        statement.setUnsetResourceMonitor(true);
        statement.setUnsetComment(true);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        assertEquals("ALTER WAREHOUSE TEST_WH UNSET RESOURCE_MONITOR, COMMENT", sqls[0].toSql());
    }
    
    // Note: Validation logic is handled by the Change classes, not the SQL generators
    // SQL generators focus purely on SQL string generation
    
    @Test
    @DisplayName("Should support Snowflake database")
    void testSupportsSnowflakeDatabase() {
        // Given
        AlterWarehouseStatement statement = new AlterWarehouseStatement();
        
        // When/Then
        assertTrue(generator.supports(statement, database));
    }
    
    @Test
    @DisplayName("Should validate warehouse name is required")
    void testValidationRequiresWarehouseName() {
        // Given
        AlterWarehouseStatement statement = new AlterWarehouseStatement();
        statement.setWarehouseSize("LARGE"); // Set operation without warehouse name
        
        // When
        ValidationErrors errors = generator.validate(statement, database, null);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().get(0).contains("Warehouse name is required"));
    }
    
    @Test 
    @DisplayName("Should validate warehouse size enumeration")
    void testValidationWarehouseSize() {
        // Given
        AlterWarehouseStatement statement = new AlterWarehouseStatement();
        statement.setWarehouseName("TEST_WH");
        statement.setWarehouseSize("INVALID_SIZE");
        
        // When
        ValidationErrors errors = generator.validate(statement, database, null);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().toString().contains("Invalid warehouse size"));
    }
    
    @Test
    @DisplayName("Should validate cluster count ranges")
    void testValidationClusterCountRanges() {
        // Given
        AlterWarehouseStatement statement = new AlterWarehouseStatement();
        statement.setWarehouseName("TEST_WH");
        statement.setMinClusterCount(15); // Invalid: > 10
        statement.setMaxClusterCount(0);  // Invalid: < 1
        
        // When
        ValidationErrors errors = generator.validate(statement, database, null);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().toString().contains("MIN_CLUSTER_COUNT must be between 1 and 10"));
        assertTrue(errors.getErrorMessages().toString().contains("MAX_CLUSTER_COUNT must be between 1 and 10"));
    }
    
    @Test
    @DisplayName("Should validate query acceleration dependency")
    void testValidationQueryAccelerationDependency() {
        // Given
        AlterWarehouseStatement statement = new AlterWarehouseStatement();
        statement.setWarehouseName("TEST_WH");
        statement.setEnableQueryAcceleration(false);
        statement.setQueryAccelerationMaxScaleFactor(8); // Invalid: acceleration disabled
        
        // When
        ValidationErrors errors = generator.validate(statement, database, null);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().get(0).contains("QUERY_ACCELERATION_MAX_SCALE_FACTOR requires ENABLE_QUERY_ACCELERATION = true"));
    }
    
    @Test
    @DisplayName("Should validate auto suspend range")
    void testValidationAutoSuspendRange() {
        // Given
        AlterWarehouseStatement statement = new AlterWarehouseStatement();
        statement.setWarehouseName("TEST_WH");
        statement.setAutoSuspend(30); // Invalid: < 60 and > 0
        
        // When
        ValidationErrors errors = generator.validate(statement, database, null);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().get(0).contains("AUTO_SUSPEND must be 0 (disabled), NULL (never), or >= 60 seconds"));
    }
}