package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AlterWarehouseStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AlterWarehouseGeneratorSnowflake
 */
@DisplayName("AlterWarehouseGeneratorSnowflake")
public class AlterWarehouseGeneratorSnowflakeTest {
    
    private AlterWarehouseGeneratorSnowflake generator;
    private AlterWarehouseStatement statement;
    
    @Mock
    private SnowflakeDatabase database;
    
    @Mock
    private SqlGeneratorChain sqlGeneratorChain;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        generator = new AlterWarehouseGeneratorSnowflake();
        statement = new AlterWarehouseStatement();
        
        // Setup database mock
        when(database.escapeObjectName("TEST_WAREHOUSE", liquibase.structure.core.Table.class))
            .thenReturn("TEST_WAREHOUSE");
        when(database.escapeObjectName("NEW_WAREHOUSE", liquibase.structure.core.Table.class))
            .thenReturn("NEW_WAREHOUSE");
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
        assertTrue(errors.getErrorMessages().get(0).contains("warehouseName is required"));
    }
    
    @Test
    @DisplayName("Should generate rename SQL")
    void shouldGenerateRenameSql() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setNewName("NEW_WAREHOUSE");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertTrue(sqls.length >= 1);
        assertEquals("ALTER WAREHOUSE TEST_WAREHOUSE RENAME TO NEW_WAREHOUSE", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate rename SQL with IF EXISTS")
    void shouldGenerateRenameSqlWithIfExists() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setIfExists(true);
        statement.setNewName("NEW_WAREHOUSE");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertTrue(sqls.length >= 1);
        assertEquals("ALTER WAREHOUSE IF EXISTS TEST_WAREHOUSE RENAME TO NEW_WAREHOUSE", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate action SQL")
    void shouldGenerateActionSql() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setAction("SUSPEND");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertTrue(sqls.length >= 1);
        assertEquals("ALTER WAREHOUSE TEST_WAREHOUSE SUSPEND", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate action SQL with different actions")
    void shouldGenerateActionSqlWithDifferentActions() {
        String[] actions = {"SUSPEND", "RESUME", "ABORT ALL QUERIES"};
        
        for (String action : actions) {
            statement = new AlterWarehouseStatement();
            statement.setWarehouseName("TEST_WAREHOUSE");
            statement.setAction(action);
            
            Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
            
            assertTrue(sqls.length >= 1);
            assertEquals("ALTER WAREHOUSE TEST_WAREHOUSE " + action, sqls[0].toSql());
        }
    }
    
    @Test
    @DisplayName("Should generate SET SQL for single property")
    void shouldGenerateSetSqlForSingleProperty() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setWarehouseSize("LARGE");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertTrue(sqls.length >= 1);
        assertTrue(sqls[0].toSql().contains("SET WAREHOUSE_SIZE = LARGE"));
    }
    
    @Test
    @DisplayName("Should generate SET SQL for multiple properties")
    void shouldGenerateSetSqlForMultipleProperties() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setWarehouseSize("XLARGE");
        statement.setAutoSuspend(300);
        statement.setAutoResume(true);
        statement.setResourceMonitor("MONTHLY_BUDGET");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertTrue(sqls.length >= 1);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("SET"));
        assertTrue(sql.contains("WAREHOUSE_SIZE = XLARGE"));
        assertTrue(sql.contains("AUTO_SUSPEND = 300"));
        assertTrue(sql.contains("AUTO_RESUME = true"));
        assertTrue(sql.contains("RESOURCE_MONITOR = MONTHLY_BUDGET"));
    }
    
    @Test
    @DisplayName("Should handle comment with quotes")
    void shouldHandleCommentWithQuotes() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setComment("Test's warehouse");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertTrue(sqls.length >= 1);
        assertTrue(sqls[0].toSql().contains("COMMENT = 'Test''s warehouse'"));
    }
    
    @Test
    @DisplayName("Should generate UNSET SQL")
    void shouldGenerateUnsetSql() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setUnsetResourceMonitor(true);
        statement.setUnsetComment(true);
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertTrue(sqls.length >= 1);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("UNSET"));
        assertTrue(sql.contains("RESOURCE_MONITOR"));
        assertTrue(sql.contains("COMMENT"));
    }
    
    @Test
    @DisplayName("Should generate separate statements for SET and UNSET")
    void shouldGenerateSeparateStatementsForSetAndUnset() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setWarehouseSize("LARGE");
        statement.setUnsetComment(true);
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Should generate at least 2 statements (SET and UNSET)
        assertTrue(sqls.length >= 2);
        
        // First should be SET
        assertTrue(sqls[0].toSql().contains("SET WAREHOUSE_SIZE = LARGE"));
        
        // Second should be UNSET (or could be USE WAREHOUSE, then UNSET)
        boolean foundUnset = false;
        for (Sql sql : sqls) {
            if (sql.toSql().contains("UNSET COMMENT")) {
                foundUnset = true;
                break;
            }
        }
        assertTrue(foundUnset);
    }
    
    @Test
    @DisplayName("Should generate SQL with all SET properties")
    void shouldGenerateSqlWithAllSetProperties() {
        statement.setWarehouseName("TEST_WAREHOUSE");
        statement.setWarehouseSize("X4LARGE");
        statement.setWarehouseType("SNOWPARK-OPTIMIZED");
        statement.setMaxClusterCount(8);
        statement.setMinClusterCount(2);
        statement.setScalingPolicy("ECONOMY");
        statement.setAutoSuspend(600);
        statement.setAutoResume(false);
        statement.setResourceMonitor("MONTHLY_BUDGET");
        statement.setComment("Production warehouse");
        statement.setEnableQueryAcceleration(true);
        statement.setQueryAccelerationMaxScaleFactor(10);
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertTrue(sqls.length >= 1);
        String sql = sqls[0].toSql();
        
        assertTrue(sql.contains("WAREHOUSE_SIZE = X4LARGE"));
        assertTrue(sql.contains("WAREHOUSE_TYPE = SNOWPARK-OPTIMIZED"));
        assertTrue(sql.contains("MAX_CLUSTER_COUNT = 8"));
        assertTrue(sql.contains("MIN_CLUSTER_COUNT = 2"));
        assertTrue(sql.contains("SCALING_POLICY = ECONOMY"));
        assertTrue(sql.contains("AUTO_SUSPEND = 600"));
        assertTrue(sql.contains("AUTO_RESUME = false"));
        assertTrue(sql.contains("RESOURCE_MONITOR = MONTHLY_BUDGET"));
        assertTrue(sql.contains("COMMENT = 'Production warehouse'"));
        assertTrue(sql.contains("ENABLE_QUERY_ACCELERATION = true"));
        assertTrue(sql.contains("QUERY_ACCELERATION_MAX_SCALE_FACTOR = 10"));
    }
}