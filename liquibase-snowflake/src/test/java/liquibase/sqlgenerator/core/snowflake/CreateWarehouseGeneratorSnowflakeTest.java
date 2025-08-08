package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.statement.core.snowflake.CreateWarehouseStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure unit tests for CreateWarehouseGeneratorSnowflake SQL generation
 * Tests SQL string output without database dependencies - NO MOCKING!
 * 
 * This demonstrates the correct pattern:
 * - Input: Statement objects with properties
 * - Output: Expected SQL strings
 * - Test: assertEquals(expectedSQL, actualSQL)
 */
@DisplayName("CreateWarehouseGeneratorSnowflake - Pure SQL Tests")
public class CreateWarehouseGeneratorSnowflakeTest {
    
    private CreateWarehouseGeneratorSnowflake generator;
    private SnowflakeDatabase database;
    
    @BeforeEach
    void setUp() {
        generator = new CreateWarehouseGeneratorSnowflake();
        database = new SnowflakeDatabase(); // Real database object, no mocking needed
    }
    
    @Test
    @DisplayName("Should generate basic CREATE WAREHOUSE SQL")
    void testBasicCreateWarehouse() {
        // Given
        CreateWarehouseStatement statement = new CreateWarehouseStatement();
        statement.setWarehouseName("TEST_WH");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        assertEquals("CREATE WAREHOUSE TEST_WH", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate CREATE OR REPLACE WAREHOUSE SQL")
    void testCreateOrReplaceWarehouse() {
        // Given
        CreateWarehouseStatement statement = new CreateWarehouseStatement();
        statement.setWarehouseName("TEST_WH");
        statement.setOrReplace(true);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        assertEquals("CREATE OR REPLACE WAREHOUSE TEST_WH", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate CREATE WAREHOUSE IF NOT EXISTS SQL")
    void testCreateWarehouseIfNotExists() {
        // Given
        CreateWarehouseStatement statement = new CreateWarehouseStatement();
        statement.setWarehouseName("TEST_WH");
        statement.setIfNotExists(true);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        assertEquals("CREATE WAREHOUSE IF NOT EXISTS TEST_WH", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate warehouse with all properties")
    void testWarehouseWithAllProperties() {
        // Given
        CreateWarehouseStatement statement = new CreateWarehouseStatement();
        statement.setWarehouseName("FULL_WH");
        statement.setWarehouseType("SNOWPARK-OPTIMIZED");
        statement.setWarehouseSize("LARGE");
        statement.setMaxClusterCount(5);
        statement.setMinClusterCount(2);
        statement.setAutoSuspend(300);
        statement.setAutoResume(true);
        statement.setResourceMonitor("MONTHLY_BUDGET");
        statement.setComment("Test warehouse with all properties");
        statement.setEnableQueryAcceleration(true);
        statement.setQueryAccelerationMaxScaleFactor(8);
        statement.setScalingPolicy("ECONOMY");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String expectedSQL = "CREATE WAREHOUSE FULL_WH WITH " +
                "WAREHOUSE_TYPE = 'SNOWPARK-OPTIMIZED' " +
                "WAREHOUSE_SIZE = LARGE " +
                "MAX_CLUSTER_COUNT = 5 " +
                "MIN_CLUSTER_COUNT = 2 " +
                "SCALING_POLICY = ECONOMY " +
                "AUTO_SUSPEND = 300 " +
                "AUTO_RESUME = true " +
                "RESOURCE_MONITOR = MONTHLY_BUDGET " +
                "COMMENT = 'Test warehouse with all properties' " +
                "ENABLE_QUERY_ACCELERATION = true " +
                "QUERY_ACCELERATION_MAX_SCALE_FACTOR = 8";
        
        assertEquals(expectedSQL, sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should validate warehouse name is required")
    void testValidationRequiresWarehouseName() {
        // Given
        CreateWarehouseStatement statement = new CreateWarehouseStatement();
        // No warehouse name set
        
        // When
        ValidationErrors errors = generator.validate(statement, database, null);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().get(0).contains("Warehouse name is required"));
    }
    
    @Test
    @DisplayName("Should support Snowflake database")
    void testSupportsSnowflakeDatabase() {
        // Given
        CreateWarehouseStatement statement = new CreateWarehouseStatement();
        
        // When/Then
        assertTrue(generator.supports(statement, database));
    }
}