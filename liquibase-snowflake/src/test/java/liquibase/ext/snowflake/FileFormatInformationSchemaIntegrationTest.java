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
 * Investigation test to understand INFORMATION_SCHEMA.FILE_FORMATS structure for FileFormat snapshot/diff requirements
 */
public class FileFormatInformationSchemaIntegrationTest {
    
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
    public void investigateFileFormatsInformationSchemaDetailed() throws Exception {
        
        List<Map<String, ?>> fileFormats = Scope.getCurrentScope().getSingleton(ExecutorService.class)
            .getExecutor("jdbc", database)
            .queryForList(new RawSqlStatement("SELECT * FROM INFORMATION_SCHEMA.FILE_FORMATS LIMIT 1"));
            
        if (!fileFormats.isEmpty()) {
            Map<String, ?> fileFormat = fileFormats.get(0);
            for (Map.Entry<String, ?> entry : fileFormat.entrySet()) {
            }
        } else {
        }
    }
    
    @Test
    public void investigateShowFileFormatsOutput() throws Exception {
        
        List<Map<String, ?>> fileFormats = Scope.getCurrentScope().getSingleton(ExecutorService.class)
            .getExecutor("jdbc", database)
            .queryForList(new RawSqlStatement("SHOW FILE FORMATS"));
            
        if (!fileFormats.isEmpty()) {
            Map<String, ?> fileFormat = fileFormats.get(0);
            for (String key : fileFormat.keySet()) {
            }
        } else {
        }
    }
    
    @Test
    public void createTestFileFormatAndInvestigate() throws Exception {
        
        // Drop if exists
        Scope.getCurrentScope().getSingleton(ExecutorService.class)
            .getExecutor("jdbc", database)
            .execute(new RawSqlStatement("DROP FILE FORMAT IF EXISTS SNAPSHOT_TEST_FF"));
            
        // Create a test file format with various attributes
        Scope.getCurrentScope().getSingleton(ExecutorService.class)
            .getExecutor("jdbc", database)
            .execute(new RawSqlStatement(
                "CREATE FILE FORMAT SNAPSHOT_TEST_FF " +
                "TYPE = CSV " +
                "COMPRESSION = GZIP " +
                "RECORD_DELIMITER = '\\n' " +
                "FIELD_DELIMITER = ',' " +
                "SKIP_HEADER = 1 " +
                "TRIM_SPACE = TRUE " +
                "ERROR_ON_COLUMN_COUNT_MISMATCH = FALSE " +
                "COMMENT = 'Test file format for snapshot investigation'"
            ));
            
        
        // Query from INFORMATION_SCHEMA.FILE_FORMATS
        List<Map<String, ?>> fileFormats = Scope.getCurrentScope().getSingleton(ExecutorService.class)
            .getExecutor("jdbc", database)
            .queryForList(new RawSqlStatement("SELECT * FROM INFORMATION_SCHEMA.FILE_FORMATS WHERE FILE_FORMAT_NAME = 'SNAPSHOT_TEST_FF'"));
            
        if (!fileFormats.isEmpty()) {
            Map<String, ?> fileFormat = fileFormats.get(0);
            for (Map.Entry<String, ?> entry : fileFormat.entrySet()) {
            }
        }
        
        // Also check SHOW FILE FORMATS output
        List<Map<String, ?>> showFileFormats = Scope.getCurrentScope().getSingleton(ExecutorService.class)
            .getExecutor("jdbc", database)
            .queryForList(new RawSqlStatement("SHOW FILE FORMATS LIKE 'SNAPSHOT_TEST_FF'"));
            
        if (!showFileFormats.isEmpty()) {
            Map<String, ?> fileFormat = showFileFormats.get(0);
            for (Map.Entry<String, ?> entry : fileFormat.entrySet()) {
            }
        }
    }
}