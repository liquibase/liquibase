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
 * Investigation test to understand INFORMATION_SCHEMA.SEQUENCES structure for sequence snapshot/diff requirements
 */
public class SequenceInvestigationTest {
    
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
    public void createTestSequenceAndInvestigate() throws Exception {
        System.out.println("=== Creating Test Sequence ===");
        
        // Drop if exists
        Scope.getCurrentScope().getSingleton(ExecutorService.class)
            .getExecutor("jdbc", database)
            .execute(new RawSqlStatement("DROP SEQUENCE IF EXISTS TESTHARNESS.SNAPSHOT_TEST_SEQ"));
            
        // Create a test sequence with various attributes
        Scope.getCurrentScope().getSingleton(ExecutorService.class)
            .getExecutor("jdbc", database)
            .execute(new RawSqlStatement(
                "CREATE SEQUENCE TESTHARNESS.SNAPSHOT_TEST_SEQ " +
                "START = 100 " +
                "INCREMENT = 5 " +
                "ORDER " +
                "COMMENT = 'Test sequence for snapshot investigation'"
            ));
            
        System.out.println("Test sequence created successfully");
        
        // Query from INFORMATION_SCHEMA.SEQUENCES
        List<Map<String, ?>> sequences = Scope.getCurrentScope().getSingleton(ExecutorService.class)
            .getExecutor("jdbc", database)
            .queryForList(new RawSqlStatement("SELECT * FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_NAME = 'SNAPSHOT_TEST_SEQ'"));
            
        if (!sequences.isEmpty()) {
            Map<String, ?> sequence = sequences.get(0);
            System.out.println("\n=== INFORMATION_SCHEMA.SEQUENCES Record ===");
            for (Map.Entry<String, ?> entry : sequence.entrySet()) {
                System.out.println(String.format("%s: %s", entry.getKey(), entry.getValue()));
            }
        }
        
        // Also check SHOW SEQUENCES output
        List<Map<String, ?>> showSequences = Scope.getCurrentScope().getSingleton(ExecutorService.class)
            .getExecutor("jdbc", database)
            .queryForList(new RawSqlStatement("SHOW SEQUENCES LIKE 'SNAPSHOT_TEST_SEQ' IN SCHEMA TESTHARNESS"));
            
        if (!showSequences.isEmpty()) {
            Map<String, ?> sequence = showSequences.get(0);
            System.out.println("\n=== SHOW SEQUENCES Record ===");
            for (Map.Entry<String, ?> entry : sequence.entrySet()) {
                System.out.println(String.format("%s: %s", entry.getKey(), entry.getValue()));
            }
        }
    }
}