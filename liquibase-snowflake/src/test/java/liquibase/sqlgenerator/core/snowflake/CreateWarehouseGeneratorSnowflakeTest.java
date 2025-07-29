package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.snowflake.CreateWarehouseStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for CreateWarehouseGeneratorSnowflake
 */
@DisplayName("CreateWarehouseGeneratorSnowflake")
public class CreateWarehouseGeneratorSnowflakeTest {
    
    private CreateWarehouseGeneratorSnowflake generator;
    private CreateWarehouseStatement statement;
    
    @Mock
    private SnowflakeDatabase database;
    
    @Mock
    private SqlGeneratorChain sqlGeneratorChain;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        generator = new CreateWarehouseGeneratorSnowflake();
        statement = new CreateWarehouseStatement();
        
        // Setup database mock
        when(database.escapeObjectName("TEST_WAREHOUSE", liquibase.structure.core.Table.class))
            .thenReturn("TEST_WAREHOUSE");
        when(database.escapeObjectName("MONTHLY_BUDGET", liquibase.structure.core.Table.class))
            .thenReturn("MONTHLY_BUDGET");
    }
    
    @Test
    @DisplayName("Should support Snowflake database")
    void shouldSupportSnowflake() {
        assertTrue(generator.supports(statement, database));
    }
    
    @Test
    @DisplayName("Should validate warehouse name is required")
    void shouldValidateWarehouseNameRequired() {
        ValidationErrors errors = generator.validate(statement, database, sqlGeneratorChain);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().get(0).contains("Warehouse name is required"));
    }
    
    @Test
    @DisplayName("Should generate basic CREATE WAREHOUSE SQL")
    void shouldGenerateBasicCreateWarehouseSql() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        assertEquals("CREATE WAREHOUSE TEST_WAREHOUSE", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate CREATE OR REPLACE WAREHOUSE SQL")
    void shouldGenerateCreateOrReplaceWarehouseSql() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setOrReplace(true);
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        assertEquals("CREATE OR REPLACE WAREHOUSE TEST_WAREHOUSE", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate CREATE WAREHOUSE IF NOT EXISTS SQL")
    void shouldGenerateCreateWarehouseIfNotExistsSql() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setIfNotExists(true);
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        assertEquals("CREATE WAREHOUSE IF NOT EXISTS TEST_WAREHOUSE", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate SQL with warehouse size")
    void shouldGenerateSqlWithWarehouseSize() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setWarehouseSize("LARGE");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("WITH"));
        assertTrue(sql.contains("WAREHOUSE_SIZE = LARGE"));
    }
    
    @Test
    @DisplayName("Should generate SQL with warehouse type")
    void shouldGenerateSqlWithWarehouseType() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setWarehouseType("SNOWPARK-OPTIMIZED");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("WAREHOUSE_TYPE = SNOWPARK-OPTIMIZED"));
        assertFalse(sql.contains("'SNOWPARK-OPTIMIZED'")); // Should not be quoted
    }
    
    @Test
    @DisplayName("Should generate SQL with multi-cluster settings")
    void shouldGenerateSqlWithMultiClusterSettings() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setMinClusterCount(1);
        statement.setMaxClusterCount(3);
        statement.setScalingPolicy("ECONOMY");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("MIN_CLUSTER_COUNT = 1"));
        assertTrue(sql.contains("MAX_CLUSTER_COUNT = 3"));
        assertTrue(sql.contains("SCALING_POLICY = ECONOMY"));
    }
    
    @Test
    @DisplayName("Should generate SQL with auto-suspend and auto-resume")
    void shouldGenerateSqlWithAutoSettings() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setAutoSuspend(300);
        statement.setAutoResume(true);
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("AUTO_SUSPEND = 300"));
        assertTrue(sql.contains("AUTO_RESUME = true"));
    }
    
    @Test
    @DisplayName("Should generate SQL with initially suspended")
    void shouldGenerateSqlWithInitiallySuspended() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setInitiallySuspended(true);
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        assertTrue(sqls[0].toSql().contains("INITIALLY_SUSPENDED = true"));
    }
    
    @Test
    @DisplayName("Should generate SQL with resource monitor")
    void shouldGenerateSqlWithResourceMonitor() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setResourceMonitor("MONTHLY_BUDGET");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        assertTrue(sqls[0].toSql().contains("RESOURCE_MONITOR = MONTHLY_BUDGET"));
    }
    
    @Test
    @DisplayName("Should generate SQL with comment")
    void shouldGenerateSqlWithComment() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setComment("Test warehouse for development");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        assertTrue(sqls[0].toSql().contains("COMMENT = 'Test warehouse for development'"));
    }
    
    @Test
    @DisplayName("Should escape quotes in comment")
    void shouldEscapeQuotesInComment() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setComment("Test's warehouse");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        assertTrue(sqls[0].toSql().contains("COMMENT = 'Test''s warehouse'"));
    }
    
    @Test
    @DisplayName("Should generate SQL with query acceleration")
    void shouldGenerateSqlWithQueryAcceleration() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setEnableQueryAcceleration(true);
        statement.setQueryAccelerationMaxScaleFactor(10);
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ENABLE_QUERY_ACCELERATION = true"));
        assertTrue(sql.contains("QUERY_ACCELERATION_MAX_SCALE_FACTOR = 10"));
    }
    
    @Test
    @DisplayName("Should generate SQL with all properties")
    void shouldGenerateSqlWithAllProperties() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setWarehouseSize("XLARGE");
        statement.setWarehouseType("STANDARD");
        statement.setMinClusterCount(2);
        statement.setMaxClusterCount(5);
        statement.setScalingPolicy("STANDARD");
        statement.setAutoSuspend(600);
        statement.setAutoResume(false);
        statement.setInitiallySuspended(false);
        statement.setResourceMonitor("MONTHLY_BUDGET");
        statement.setComment("Production warehouse");
        statement.setEnableQueryAcceleration(true);
        statement.setQueryAccelerationMaxScaleFactor(8);
        statement.setMaxConcurrencyLevel(16);
        statement.setStatementQueuedTimeoutInSeconds(300);
        statement.setStatementTimeoutInSeconds(3600);
        statement.setResourceConstraint("MEMORY_2X");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        
        // Verify all properties are included
        assertTrue(sql.contains("WAREHOUSE_TYPE = STANDARD"));
        assertTrue(sql.contains("WAREHOUSE_SIZE = XLARGE"));
        assertTrue(sql.contains("MIN_CLUSTER_COUNT = 2"));
        assertTrue(sql.contains("MAX_CLUSTER_COUNT = 5"));
        assertTrue(sql.contains("SCALING_POLICY = STANDARD"));
        assertTrue(sql.contains("AUTO_SUSPEND = 600"));
        assertTrue(sql.contains("AUTO_RESUME = false"));
        assertTrue(sql.contains("INITIALLY_SUSPENDED = false"));
        assertTrue(sql.contains("RESOURCE_MONITOR = MONTHLY_BUDGET"));
        assertTrue(sql.contains("RESOURCE_CONSTRAINT = MEMORY_2X"));
        assertTrue(sql.contains("COMMENT = 'Production warehouse'"));
        assertTrue(sql.contains("ENABLE_QUERY_ACCELERATION = true"));
        assertTrue(sql.contains("QUERY_ACCELERATION_MAX_SCALE_FACTOR = 8"));
        assertTrue(sql.contains("MAX_CONCURRENCY_LEVEL = 16"));
        assertTrue(sql.contains("STATEMENT_QUEUED_TIMEOUT_IN_SECONDS = 300"));
        assertTrue(sql.contains("STATEMENT_TIMEOUT_IN_SECONDS = 3600"));
    }
    
    @Test
    @DisplayName("Should format WITH clause correctly")
    void shouldFormatWithClauseCorrectly() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setWarehouseSize("MEDIUM");
        statement.setAutoSuspend(300);
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        
        // Should have WITH keyword and proper spacing
        assertTrue(sql.matches(".*WITH\\s+WAREHOUSE_SIZE.*"));
        // Properties should be space-separated, not comma-separated
        assertTrue(sql.contains("WAREHOUSE_SIZE = MEDIUM AUTO_SUSPEND = 300"));
    }
}