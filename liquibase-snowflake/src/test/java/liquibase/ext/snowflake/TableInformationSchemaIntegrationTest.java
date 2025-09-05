package liquibase.ext.snowflake;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.RawSqlStatement;
import liquibase.util.TestDatabaseConfigUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

/**
 * Investigation test to understand INFORMATION_SCHEMA.TABLES structure for table snapshot/diff requirements
 */
public class TableInformationSchemaIntegrationTest {
    
    private Database database;
    private String testSchema = "TABLE_INFO_SCHEMA_TEST";
    
    @BeforeEach
    public void setUp() throws Exception {
        // Use YAML configuration instead of environment variables
        String url = TestDatabaseConfigUtil.getSnowflakeUrl();
        String username = TestDatabaseConfigUtil.getSnowflakeUsername();
        String password = TestDatabaseConfigUtil.getSnowflakePassword();
        
        database = DatabaseFactory.getInstance().openDatabase(url, username, password, null, null);
        
        // Create clean test schema using schema isolation pattern
        Scope.getCurrentScope().getSingleton(ExecutorService.class)
            .getExecutor("jdbc", database)
            .execute(new RawSqlStatement("DROP SCHEMA IF EXISTS " + testSchema + " CASCADE"));
        Scope.getCurrentScope().getSingleton(ExecutorService.class)
            .getExecutor("jdbc", database)
            .execute(new RawSqlStatement("CREATE SCHEMA " + testSchema));
        Scope.getCurrentScope().getSingleton(ExecutorService.class)
            .getExecutor("jdbc", database)
            .execute(new RawSqlStatement("USE SCHEMA " + testSchema));
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        if (database != null) {
            // Clean up test schema using schema isolation pattern
            try {
                Scope.getCurrentScope().getSingleton(ExecutorService.class)
                    .getExecutor("jdbc", database)
                    .execute(new RawSqlStatement("DROP SCHEMA IF EXISTS " + testSchema + " CASCADE"));
            } catch (Exception e) {
                // Ignore cleanup errors
            }
            database.close();
        }
    }
    
    @Test
    public void investigateTablesInformationSchemaDetailed() throws Exception {
        
        List<Map<String, ?>> tables = Scope.getCurrentScope().getSingleton(ExecutorService.class)
            .getExecutor("jdbc", database)
            .queryForList(new RawSqlStatement("SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + testSchema + "' LIMIT 1"));
            
        if (!tables.isEmpty()) {
            Map<String, ?> table = tables.get(0);
            for (Map.Entry<String, ?> entry : table.entrySet()) {
            }
        } else {
        }
    }
    
    @Test
    public void investigateShowTablesOutput() throws Exception {
        
        List<Map<String, ?>> tables = Scope.getCurrentScope().getSingleton(ExecutorService.class)
            .getExecutor("jdbc", database)
            .queryForList(new RawSqlStatement("SHOW TABLES IN SCHEMA " + testSchema));
            
        if (!tables.isEmpty()) {
            Map<String, ?> table = tables.get(0);
            for (String key : table.keySet()) {
            }
        } else {
        }
    }
    
    @Test
    public void createTestTable() throws Exception {
        
        // Create a test table with various Snowflake-specific attributes
        Scope.getCurrentScope().getSingleton(ExecutorService.class)
            .getExecutor("jdbc", database)
            .execute(new RawSqlStatement("DROP TABLE IF EXISTS " + testSchema + ".SNAPSHOT_TEST_TABLE"));
            
        Scope.getCurrentScope().getSingleton(ExecutorService.class)
            .getExecutor("jdbc", database)
            .execute(new RawSqlStatement(
                "CREATE TABLE " + testSchema + ".SNAPSHOT_TEST_TABLE (" +
                "  ID NUMBER," +
                "  NAME STRING," +
                "  CREATED_DATE DATE" +
                ") " +
                "CLUSTER BY (ID) " +
                "DATA_RETENTION_TIME_IN_DAYS = 7 " +
                "CHANGE_TRACKING = TRUE " +
                "COMMENT = 'Test table for snapshot investigation'"
            ));
            
        
        // Now query it from INFORMATION_SCHEMA
        List<Map<String, ?>> tables = Scope.getCurrentScope().getSingleton(ExecutorService.class)
            .getExecutor("jdbc", database)
            .queryForList(new RawSqlStatement("SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'SNAPSHOT_TEST_TABLE'"));
            
        if (!tables.isEmpty()) {
            Map<String, ?> table = tables.get(0);
            for (Map.Entry<String, ?> entry : table.entrySet()) {
            }
        }
        
        // Also check SHOW TABLES output
        List<Map<String, ?>> showTables = Scope.getCurrentScope().getSingleton(ExecutorService.class)
            .getExecutor("jdbc", database)
            .queryForList(new RawSqlStatement("SHOW TABLES LIKE 'SNAPSHOT_TEST_TABLE' IN SCHEMA " + testSchema));
            
        if (!showTables.isEmpty()) {
            Map<String, ?> table = showTables.get(0);
            for (Map.Entry<String, ?> entry : table.entrySet()) {
            }
        }
    }
}