package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.snowflake.DropWarehouseStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Integration test for DropWarehouseGeneratorSnowflake that tests all variations
 * against a live Snowflake database following test harness patterns.
 * 
 * Uses the same configuration as test harness: harness-config.local.yml
 * Implements schema isolation similar to SchemaIsolationHook for test safety.
 */
@DisplayName("DropWarehouseGeneratorSnowflake Integration Tests")
public class DropWarehouseGeneratorSnowflakeIntegrationTest {
    
    private DropWarehouseGeneratorSnowflake generator;
    private Database database;
    private Connection rawConnection;
    private String testSchemaName;
    private List<String> createdWarehouses;
    private String originalSchema;
    
    /**
     * Generate unique warehouse name based on test method name to avoid conflicts
     * when running tests in parallel.
     */
    private String getUniqueWarehouseName(String methodName) {
        return "TEST_DROP_" + methodName;
    }
    
    @Mock
    private SqlGeneratorChain sqlGeneratorChain;
    
    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        generator = new DropWarehouseGeneratorSnowflake();
        createdWarehouses = new ArrayList<>();
        
        // Follow test harness configuration pattern - check for required config
        String url = System.getenv("SNOWFLAKE_URL");
        String username = System.getenv("SNOWFLAKE_USER");
        String password = System.getenv("SNOWFLAKE_PASSWORD");
        
        // Skip tests if not configured (like test harness does with assumeTrue)
        Assumptions.assumeTrue(url != null && username != null && password != null, 
                              "Snowflake test not configured");
        
        // Initialize database connection following DatabaseConnectionUtil pattern
        try {
            rawConnection = DriverManager.getConnection(url, username, password);
            JdbcConnection jdbcConnection = new JdbcConnection(rawConnection);
            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConnection);
            
            assertTrue(database instanceof SnowflakeDatabase, "Not a Snowflake database");
            
            // Force release lock like test harness does
            liquibase.lockservice.LockServiceFactory.getInstance().getLockService(database).forceReleaseLock();
            
            // Set output flags like test harness does
            database.setOutputDefaultCatalog(false);
            database.setOutputDefaultSchema(false);
            
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "Cannot connect to Snowflake: " + e.getMessage());
            return;
        }
        
        // Implement schema isolation following SchemaIsolationHook pattern
        testSchemaName = "TEST_DROP_WAREHOUSE_INTEGRATION";
        
        try {
            // Store original schema before switching
            try (Statement stmt = rawConnection.createStatement()) {
                java.sql.ResultSet rs = stmt.executeQuery("SELECT CURRENT_SCHEMA()");
                if (rs.next()) {
                    originalSchema = rs.getString(1);
                }
                rs.close();
            }
            
            // Create isolated schema (like SchemaIsolationHook does)
            System.out.println("SchemaIsolationHook: Setting up isolated schema: " + testSchemaName);
            try (Statement stmt = rawConnection.createStatement()) {
                stmt.execute("CREATE SCHEMA IF NOT EXISTS " + testSchemaName);
                stmt.execute("USE SCHEMA " + testSchemaName);
            }
            
            // Update database to use isolated schema (like SchemaIsolationHook does)
            database.setDefaultSchemaName(testSchemaName);
            System.out.println("SchemaIsolationHook: New default schema: " + database.getDefaultSchemaName());
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up isolated schema: " + testSchemaName, e);
        }
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (rawConnection != null && !rawConnection.isClosed()) {
            try (Statement stmt = rawConnection.createStatement()) {
                // Drop any remaining test warehouses (shouldn't be many since we're testing DROP)
                for (String warehouseName : createdWarehouses) {
                    try {
                        stmt.execute("DROP WAREHOUSE IF EXISTS " + warehouseName);
                        System.out.println("Cleaned up remaining warehouse: " + warehouseName);
                    } catch (Exception e) {
                        System.err.println("Failed to cleanup warehouse " + warehouseName + ": " + e.getMessage());
                    }
                }
                
                // Switch back to original schema before dropping test schema (like SchemaIsolationHook does)
                if (originalSchema != null) {
                    try {
                        stmt.execute("USE SCHEMA " + originalSchema);
                        System.out.println("SchemaIsolationHook: Switched back to original schema: " + originalSchema);
                    } catch (Exception e) {
                        System.err.println("Failed to switch back to original schema: " + e.getMessage());
                    }
                }
                
                // Drop test schema (like SchemaIsolationHook cleanup)
                try {
                    stmt.execute("DROP SCHEMA IF EXISTS " + testSchemaName);
                    System.out.println("SchemaIsolationHook: Cleaned up schema: " + testSchemaName);
                } catch (Exception e) {
                    System.err.println("Failed to cleanup schema " + testSchemaName + ": " + e.getMessage());
                }
            }
            rawConnection.close();
        }
    }
    
    private void createTestWarehouse(String warehouseName) throws Exception {
        try (Statement stmt = rawConnection.createStatement()) {
            stmt.execute("CREATE WAREHOUSE " + warehouseName);
            createdWarehouses.add(warehouseName);
            System.out.println("Created test warehouse for dropping: " + warehouseName);
        }
    }
    
    private void executeAndVerifyDrop(DropWarehouseStatement statement, String testName) throws Exception {
        // Generate SQL
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        assertEquals(1, sqls.length, "Should generate exactly one SQL statement");
        
        String sql = sqls[0].toSql();
        System.out.println("Testing " + testName + ": " + sql);
        
        // Execute against Snowflake using rawConnection
        try (Statement stmt = rawConnection.createStatement()) {
            stmt.execute(sql);
            // Remove from our cleanup list since it's now dropped
            createdWarehouses.remove(statement.getWarehouseName());
            System.out.println("✅ SUCCESS: " + testName);
        } catch (Exception e) {
            System.err.println("❌ FAILED: " + testName + " - " + e.getMessage());
            throw new AssertionError("SQL execution failed for " + testName + ": " + sql, e);
        }
    }
    
    @Test
    @DisplayName("Integration: Basic DROP WAREHOUSE")
    void testBasicDrop() throws Exception {
        String warehouseName = getUniqueWarehouseName("testBasicDrop");
        
        // Create the warehouse to drop
        createTestWarehouse(warehouseName);
        
        DropWarehouseStatement statement = new DropWarehouseStatement();
        statement.setWarehouseName(warehouseName);
        
        executeAndVerifyDrop(statement, "Basic DROP WAREHOUSE");
    }
    
    @Test
    @DisplayName("Integration: DROP WAREHOUSE IF EXISTS - Exists")
    void testDropIfExistsWhenExists() throws Exception {
        String warehouseName = getUniqueWarehouseName("testDropIfExistsWhenExists");
        
        // Create the warehouse to drop
        createTestWarehouse(warehouseName);
        
        DropWarehouseStatement statement = new DropWarehouseStatement();
        statement.setWarehouseName(warehouseName);
        statement.setIfExists(true);
        
        executeAndVerifyDrop(statement, "DROP WAREHOUSE IF EXISTS - Exists");
    }
    
    @Test
    @DisplayName("Integration: DROP WAREHOUSE IF EXISTS - Does Not Exist")
    void testDropIfExistsWhenDoesNotExist() throws Exception {
        String warehouseName = getUniqueWarehouseName("testDropIfExistsWhenDoesNotExist");
        
        // Don't create the warehouse - it doesn't exist
        
        DropWarehouseStatement statement = new DropWarehouseStatement();
        statement.setWarehouseName(warehouseName);
        statement.setIfExists(true);
        
        // This should succeed without error since IF EXISTS is specified
        executeAndVerifyDrop(statement, "DROP WAREHOUSE IF EXISTS - Does Not Exist");
    }
    
    @Test
    @DisplayName("Integration: DROP WAREHOUSE - Complex Name")
    void testDropComplexName() throws Exception {
        String warehouseName = getUniqueWarehouseName("testDropComplexName");
        
        // Create the warehouse to drop
        createTestWarehouse(warehouseName);
        
        DropWarehouseStatement statement = new DropWarehouseStatement();
        statement.setWarehouseName(warehouseName);
        
        executeAndVerifyDrop(statement, "DROP WAREHOUSE - Complex Name");
    }
    
    @Test
    @DisplayName("Integration: DROP WAREHOUSE - Running Warehouse")
    void testDropRunningWarehouse() throws Exception {
        String warehouseName = getUniqueWarehouseName("testDropRunningWarehouse");
        
        // Create and ensure the warehouse is running
        try (Statement stmt = rawConnection.createStatement()) {
            stmt.execute("CREATE WAREHOUSE " + warehouseName + " WITH INITIALLY_SUSPENDED = false");
            createdWarehouses.add(warehouseName);
        }
        
        DropWarehouseStatement statement = new DropWarehouseStatement();
        statement.setWarehouseName(warehouseName);
        
        // Should succeed - Snowflake automatically suspends running warehouses before dropping
        executeAndVerifyDrop(statement, "DROP WAREHOUSE - Running Warehouse");
    }
    
    @Test
    @DisplayName("Integration: DROP WAREHOUSE - Suspended Warehouse")
    void testDropSuspendedWarehouse() throws Exception {
        String warehouseName = getUniqueWarehouseName("testDropSuspendedWarehouse");
        
        // Create and ensure the warehouse is suspended
        try (Statement stmt = rawConnection.createStatement()) {
            stmt.execute("CREATE WAREHOUSE " + warehouseName + " WITH INITIALLY_SUSPENDED = true");
            createdWarehouses.add(warehouseName);
        }
        
        DropWarehouseStatement statement = new DropWarehouseStatement();
        statement.setWarehouseName(warehouseName);
        
        executeAndVerifyDrop(statement, "DROP WAREHOUSE - Suspended Warehouse");
    }
    
    @Test
    @DisplayName("Integration: DROP WAREHOUSE - With Properties")
    void testDropWarehouseWithProperties() throws Exception {
        String warehouseName = getUniqueWarehouseName("testDropWarehouseWithProperties");
        
        // Create warehouse with various properties
        try (Statement stmt = rawConnection.createStatement()) {
            stmt.execute("CREATE WAREHOUSE " + warehouseName + 
                        " WITH WAREHOUSE_SIZE = 'LARGE' " +
                        "AUTO_SUSPEND = 300 " +
                        "AUTO_RESUME = true " +
                        "COMMENT = 'Test warehouse with properties'");
            createdWarehouses.add(warehouseName);
        }
        
        DropWarehouseStatement statement = new DropWarehouseStatement();
        statement.setWarehouseName(warehouseName);
        
        executeAndVerifyDrop(statement, "DROP WAREHOUSE - With Properties");
    }
    
    @Test
    @DisplayName("Integration: Validation - Missing Warehouse Name")
    void testValidationMissingWarehouseName() {
        DropWarehouseStatement statement = new DropWarehouseStatement();
        // Don't set warehouse name
        
        ValidationErrors errors = generator.validate(statement, database, sqlGeneratorChain);
        
        assertTrue(errors.hasErrors(), "Should have validation errors for missing warehouse name");
        assertTrue(errors.getErrorMessages().get(0).contains("Warehouse name is required"), 
                  "Should specify warehouse name is required");
    }
    
    @Test
    @DisplayName("Integration: Error Handling - DROP Non-existent Warehouse Without IF EXISTS")
    void testDropNonExistentWithoutIfExists() {
        String warehouseName = getUniqueWarehouseName("testDropNonExistentWithoutIfExists");
        
        // Don't create the warehouse - it doesn't exist
        
        DropWarehouseStatement statement = new DropWarehouseStatement();
        statement.setWarehouseName(warehouseName);
        // Don't set IF EXISTS - should fail
        
        // Generate SQL
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        assertEquals(1, sqls.length, "Should generate exactly one SQL statement");
        
        String sql = sqls[0].toSql();
        System.out.println("Testing Error Handling - DROP Non-existent: " + sql);
        
        // This should fail with an error
        try (Statement stmt = rawConnection.createStatement()) {
            Exception exception = assertThrows(Exception.class, () -> {
                stmt.execute(sql);
            });
            System.out.println("✅ SUCCESS: Expected error occurred - " + exception.getMessage());
            assertTrue(exception.getMessage().toLowerCase().contains("does not exist") || 
                      exception.getMessage().toLowerCase().contains("not found"),
                      "Should get 'does not exist' or 'not found' error");
        } catch (Exception e) {
            throw new AssertionError("Unexpected error during error handling test", e);
        }
    }
    
    @Test
    @DisplayName("Integration: Verify Schema Isolation")
    void testSchemaIsolation() throws Exception {
        // Verify we're in our isolated schema (following SchemaIsolationHook pattern)
        try (Statement stmt = rawConnection.createStatement()) {
            java.sql.ResultSet rs = stmt.executeQuery("SELECT CURRENT_SCHEMA()");
            if (rs.next()) {
                String currentSchema = rs.getString(1);
                System.out.println("Current schema: " + currentSchema);
                assertEquals(testSchemaName, currentSchema, "Should be in isolated test schema");
            }
            rs.close();
        }
        
        // Create and drop a test warehouse to ensure isolation works
        String warehouseName = getUniqueWarehouseName("testSchemaIsolation");
        createTestWarehouse(warehouseName);
        
        DropWarehouseStatement statement = new DropWarehouseStatement();
        statement.setWarehouseName(warehouseName);
        
        executeAndVerifyDrop(statement, "Schema Isolation Test");
    }
}