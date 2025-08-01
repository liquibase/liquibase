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
    @DisplayName("Should generate basic CREATE WAREHOUSE SQL - Basic Required Only")
    void shouldGenerateBasicCreateWarehouseSql() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        assertEquals("CREATE WAREHOUSE TEST_WAREHOUSE", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate CREATE OR REPLACE WAREHOUSE SQL - OR REPLACE")
    void shouldGenerateCreateOrReplaceWarehouseSql() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setOrReplace(true);
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        assertEquals("CREATE OR REPLACE WAREHOUSE TEST_WAREHOUSE", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate CREATE WAREHOUSE IF NOT EXISTS SQL - IF NOT EXISTS")
    void shouldGenerateCreateWarehouseIfNotExistsSql() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setIfNotExists(true);
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        assertEquals("CREATE WAREHOUSE IF NOT EXISTS TEST_WAREHOUSE", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate SQL with warehouse size - With Warehouse Size")
    void shouldGenerateSqlWithWarehouseSize() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setWarehouseSize("LARGE");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        assertEquals("CREATE WAREHOUSE TEST_WAREHOUSE WITH WAREHOUSE_SIZE = LARGE", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate SQL with warehouse type - With Warehouse Type")
    void shouldGenerateSqlWithWarehouseType() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setWarehouseType("SNOWPARK-OPTIMIZED");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        assertEquals("CREATE WAREHOUSE TEST_WAREHOUSE WITH WAREHOUSE_TYPE = SNOWPARK-OPTIMIZED", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate SQL with multi-cluster settings - Multi-cluster Basic")
    void shouldGenerateSqlWithMultiClusterSettings() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setMinClusterCount(1);
        statement.setMaxClusterCount(3);
        statement.setScalingPolicy("ECONOMY");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        assertEquals("CREATE WAREHOUSE TEST_WAREHOUSE WITH MAX_CLUSTER_COUNT = 3 MIN_CLUSTER_COUNT = 1 SCALING_POLICY = ECONOMY", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate SQL with auto-suspend and auto-resume - Auto Settings")
    void shouldGenerateSqlWithAutoSettings() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setAutoSuspend(300);
        statement.setAutoResume(true);
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        assertEquals("CREATE WAREHOUSE TEST_WAREHOUSE WITH AUTO_SUSPEND = 300 AUTO_RESUME = true", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate SQL with initially suspended - Initially Suspended")
    void shouldGenerateSqlWithInitiallySuspended() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setInitiallySuspended(true);
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        assertEquals("CREATE WAREHOUSE TEST_WAREHOUSE WITH INITIALLY_SUSPENDED = true", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate SQL with resource monitor - With Resource Monitor")
    void shouldGenerateSqlWithResourceMonitor() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setResourceMonitor("MONTHLY_BUDGET");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        assertEquals("CREATE WAREHOUSE TEST_WAREHOUSE WITH RESOURCE_MONITOR = MONTHLY_BUDGET", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate SQL with comment - With Comment")
    void shouldGenerateSqlWithComment() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setComment("Test warehouse for development");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        assertEquals("CREATE WAREHOUSE TEST_WAREHOUSE WITH COMMENT = 'Test warehouse for development'", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should escape quotes in comment - Special Characters in Comment")
    void shouldEscapeQuotesInComment() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setComment("Test's warehouse");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        assertEquals("CREATE WAREHOUSE TEST_WAREHOUSE WITH COMMENT = 'Test''s warehouse'", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate SQL with query acceleration - Query Acceleration")
    void shouldGenerateSqlWithQueryAcceleration() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setEnableQueryAcceleration(true);
        statement.setQueryAccelerationMaxScaleFactor(10);
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        assertEquals("CREATE WAREHOUSE TEST_WAREHOUSE WITH ENABLE_QUERY_ACCELERATION = true QUERY_ACCELERATION_MAX_SCALE_FACTOR = 10", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate SQL with all properties - All Properties")
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
        statement.setResourceConstraint("MEMORY_16X");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        assertEquals("CREATE WAREHOUSE TEST_WAREHOUSE WITH WAREHOUSE_TYPE = STANDARD WAREHOUSE_SIZE = XLARGE MAX_CLUSTER_COUNT = 5 MIN_CLUSTER_COUNT = 2 SCALING_POLICY = STANDARD AUTO_SUSPEND = 600 AUTO_RESUME = false INITIALLY_SUSPENDED = false RESOURCE_MONITOR = MONTHLY_BUDGET RESOURCE_CONSTRAINT = MEMORY_16X COMMENT = 'Production warehouse' ENABLE_QUERY_ACCELERATION = true QUERY_ACCELERATION_MAX_SCALE_FACTOR = 8 MAX_CONCURRENCY_LEVEL = 16 STATEMENT_QUEUED_TIMEOUT_IN_SECONDS = 300 STATEMENT_TIMEOUT_IN_SECONDS = 3600", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should format WITH clause correctly - WITH Clause Format")
    void shouldFormatWithClauseCorrectly() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setWarehouseSize("MEDIUM");
        statement.setAutoSuspend(300);
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        assertEquals("CREATE WAREHOUSE TEST_WAREHOUSE WITH WAREHOUSE_SIZE = MEDIUM AUTO_SUSPEND = 300", sqls[0].toSql());
    }
}