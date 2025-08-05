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
 * Investigation test to understand INFORMATION_SCHEMA structure for snapshot/diff requirements
 */
public class InformationSchemaInvestigationTest {
    
    private Database database;
    
    @BeforeEach
    public void setUp() throws Exception {
        // Use YAML configuration instead of environment variables
        String url = TestDatabaseConfigUtil.getSnowflakeUrl();
        String username = TestDatabaseConfigUtil.getSnowflakeUsername();
        String password = TestDatabaseConfigUtil.getSnowflakePassword();
        
        database = DatabaseFactory.getInstance().openDatabase(url, username, password, null, null);
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        if (database != null) {
            database.close();
        }
    }
    
    @Test
    public void investigateDatabasesInformationSchema() throws Exception {
        System.out.println("=== INFORMATION_SCHEMA.DATABASES Structure ===");
        
        List<Map<String, ?>> columns = Scope.getCurrentScope().getSingleton(ExecutorService.class)
            .getExecutor("jdbc", database)
            .queryForList(new RawSqlStatement("DESCRIBE TABLE INFORMATION_SCHEMA.DATABASES"));
            
        for (Map<String, ?> column : columns) {
            System.out.println(String.format("Column: %s, Type: %s, Nullable: %s", 
                column.get("name"), column.get("type"), column.get("null?")));
        }
        
        System.out.println("\n=== Sample Database Record ===");
        List<Map<String, ?>> databases = Scope.getCurrentScope().getSingleton(ExecutorService.class)
            .getExecutor("jdbc", database)
            .queryForList(new RawSqlStatement("SELECT * FROM INFORMATION_SCHEMA.DATABASES WHERE DATABASE_NAME = 'LTHDB' LIMIT 1"));
            
        if (!databases.isEmpty()) {
            Map<String, ?> db = databases.get(0);
            for (Map.Entry<String, ?> entry : db.entrySet()) {
                System.out.println(String.format("%s: %s", entry.getKey(), entry.getValue()));
            }
        }
    }
    
    @Test
    public void investigateSequencesInformationSchema() throws Exception {
        System.out.println("=== INFORMATION_SCHEMA.SEQUENCES Structure ===");
        
        List<Map<String, ?>> columns = Scope.getCurrentScope().getSingleton(ExecutorService.class)
            .getExecutor("jdbc", database)
            .queryForList(new RawSqlStatement("DESCRIBE TABLE INFORMATION_SCHEMA.SEQUENCES"));
            
        for (Map<String, ?> column : columns) {
            System.out.println(String.format("Column: %s, Type: %s, Nullable: %s", 
                column.get("name"), column.get("type"), column.get("null?")));
        }
    }
    
    @Test
    public void investigateSchemataInformationSchema() throws Exception {
        System.out.println("=== INFORMATION_SCHEMA.SCHEMATA Structure ===");
        
        List<Map<String, ?>> columns = Scope.getCurrentScope().getSingleton(ExecutorService.class)
            .getExecutor("jdbc", database)
            .queryForList(new RawSqlStatement("DESCRIBE TABLE INFORMATION_SCHEMA.SCHEMATA"));
            
        for (Map<String, ?> column : columns) {
            System.out.println(String.format("Column: %s, Type: %s, Nullable: %s", 
                column.get("name"), column.get("type"), column.get("null?")));
        }
        
        System.out.println("\n=== Sample Schema Record ===");
        List<Map<String, ?>> schemas = Scope.getCurrentScope().getSingleton(ExecutorService.class)
            .getExecutor("jdbc", database)
            .queryForList(new RawSqlStatement("SELECT * FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = 'TESTHARNESS' LIMIT 1"));
            
        if (!schemas.isEmpty()) {
            Map<String, ?> schema = schemas.get(0);
            for (Map.Entry<String, ?> entry : schema.entrySet()) {
                System.out.println(String.format("%s: %s", entry.getKey(), entry.getValue()));
            }
        }
    }
    
    @Test
    public void investigateTablesInformationSchema() throws Exception {
        System.out.println("=== INFORMATION_SCHEMA.TABLES Structure ===");
        
        List<Map<String, ?>> columns = Scope.getCurrentScope().getSingleton(ExecutorService.class)
            .getExecutor("jdbc", database)
            .queryForList(new RawSqlStatement("DESCRIBE TABLE INFORMATION_SCHEMA.TABLES"));
            
        for (Map<String, ?> column : columns) {
            System.out.println(String.format("Column: %s, Type: %s, Nullable: %s", 
                column.get("name"), column.get("type"), column.get("null?")));
        }
    }
    
    @Test
    public void investigateShowDatabasesOutput() throws Exception {
        System.out.println("=== SHOW DATABASES Output Structure ===");
        
        List<Map<String, ?>> databases = Scope.getCurrentScope().getSingleton(ExecutorService.class)
            .getExecutor("jdbc", database)
            .queryForList(new RawSqlStatement("SHOW DATABASES"));
            
        if (!databases.isEmpty()) {
            Map<String, ?> db = databases.get(0);
            System.out.println("SHOW DATABASES columns:");
            for (String key : db.keySet()) {
                System.out.println(String.format("Column: %s, Value: %s", key, db.get(key)));
            }
        }
    }
    
    @Test
    public void investigateShowSequencesOutput() throws Exception {
        System.out.println("=== SHOW SEQUENCES Output Structure ===");
        
        List<Map<String, ?>> sequences = Scope.getCurrentScope().getSingleton(ExecutorService.class)
            .getExecutor("jdbc", database)
            .queryForList(new RawSqlStatement("SHOW SEQUENCES"));
            
        if (!sequences.isEmpty()) {
            Map<String, ?> seq = sequences.get(0);
            System.out.println("SHOW SEQUENCES columns:");
            for (String key : seq.keySet()) {
                System.out.println(String.format("Column: %s, Value: %s", key, seq.get(key)));
            }
        }
    }
}