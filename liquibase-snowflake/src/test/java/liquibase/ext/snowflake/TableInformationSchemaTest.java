package liquibase.ext.snowflake;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.RawSqlStatement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

/**
 * Investigation test to understand INFORMATION_SCHEMA.TABLES structure for table snapshot/diff requirements
 */
public class TableInformationSchemaTest {
    
    private Database database;
    
    @BeforeEach
    public void setUp() throws Exception {
        String url = System.getenv("SNOWFLAKE_URL");
        String username = System.getenv("SNOWFLAKE_USER");
        String password = System.getenv("SNOWFLAKE_PASSWORD");
        
        if (url == null || username == null || password == null) {
            throw new RuntimeException("Missing Snowflake credentials");
        }
        
        database = DatabaseFactory.getInstance().openDatabase(url, username, password, null, null);
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        if (database != null) {
            database.close();
        }
    }
    
    @Test
    public void investigateTablesInformationSchemaDetailed() throws Exception {
        System.out.println("=== INFORMATION_SCHEMA.TABLES Sample Record ===");
        
        List<Map<String, ?>> tables = Scope.getCurrentScope().getSingleton(ExecutorService.class)
            .getExecutor("jdbc", database)
            .queryForList(new RawSqlStatement("SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'TESTHARNESS' LIMIT 1"));
            
        if (!tables.isEmpty()) {
            Map<String, ?> table = tables.get(0);
            for (Map.Entry<String, ?> entry : table.entrySet()) {
                System.out.println(String.format("%s: %s", entry.getKey(), entry.getValue()));
            }
        } else {
            System.out.println("No tables found in TESTHARNESS schema");
        }
    }
    
    @Test
    public void investigateShowTablesOutput() throws Exception {
        System.out.println("=== SHOW TABLES Output Structure ===");
        
        List<Map<String, ?>> tables = Scope.getCurrentScope().getSingleton(ExecutorService.class)
            .getExecutor("jdbc", database)
            .queryForList(new RawSqlStatement("SHOW TABLES IN SCHEMA TESTHARNESS"));
            
        if (!tables.isEmpty()) {
            Map<String, ?> table = tables.get(0);
            System.out.println("SHOW TABLES columns:");
            for (String key : table.keySet()) {
                System.out.println(String.format("Column: %s, Value: %s", key, table.get(key)));
            }
        } else {
            System.out.println("No tables found in TESTHARNESS schema");
        }
    }
    
    @Test
    public void createTestTable() throws Exception {
        System.out.println("=== Creating Test Table ===");
        
        // Create a test table with various Snowflake-specific attributes
        Scope.getCurrentScope().getSingleton(ExecutorService.class)
            .getExecutor("jdbc", database)
            .execute(new RawSqlStatement("DROP TABLE IF EXISTS TESTHARNESS.SNAPSHOT_TEST_TABLE"));
            
        Scope.getCurrentScope().getSingleton(ExecutorService.class)
            .getExecutor("jdbc", database)
            .execute(new RawSqlStatement(
                "CREATE TABLE TESTHARNESS.SNAPSHOT_TEST_TABLE (" +
                "  ID NUMBER," +
                "  NAME STRING," +
                "  CREATED_DATE DATE" +
                ") " +
                "CLUSTER BY (ID) " +
                "DATA_RETENTION_TIME_IN_DAYS = 7 " +
                "CHANGE_TRACKING = TRUE " +
                "COMMENT = 'Test table for snapshot investigation'"
            ));
            
        System.out.println("Test table created successfully");
        
        // Now query it from INFORMATION_SCHEMA
        List<Map<String, ?>> tables = Scope.getCurrentScope().getSingleton(ExecutorService.class)
            .getExecutor("jdbc", database)
            .queryForList(new RawSqlStatement("SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'SNAPSHOT_TEST_TABLE'"));
            
        if (!tables.isEmpty()) {
            Map<String, ?> table = tables.get(0);
            System.out.println("\n=== Created Table in INFORMATION_SCHEMA ===");
            for (Map.Entry<String, ?> entry : table.entrySet()) {
                System.out.println(String.format("%s: %s", entry.getKey(), entry.getValue()));
            }
        }
        
        // Also check SHOW TABLES output
        List<Map<String, ?>> showTables = Scope.getCurrentScope().getSingleton(ExecutorService.class)
            .getExecutor("jdbc", database)
            .queryForList(new RawSqlStatement("SHOW TABLES LIKE 'SNAPSHOT_TEST_TABLE' IN SCHEMA TESTHARNESS"));
            
        if (!showTables.isEmpty()) {
            Map<String, ?> table = showTables.get(0);
            System.out.println("\n=== Created Table in SHOW TABLES ===");
            for (Map.Entry<String, ?> entry : table.entrySet()) {
                System.out.println(String.format("%s: %s", entry.getKey(), entry.getValue()));
            }
        }
    }
}