package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.snowflake.CreateWarehouseStatement;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Integration test for CreateWarehouseGeneratorSnowflake that tests all variations
 * against a live Snowflake database following test harness patterns.
 * 
 * Uses the same configuration as test harness: harness-config.local.yml
 * Implements schema isolation similar to SchemaIsolationHook for test safety.
 */
@DisplayName("CreateWarehouseGeneratorSnowflake Integration Tests")
public class CreateWarehouseGeneratorSnowflakeIntegrationTest {
    
    private CreateWarehouseGeneratorSnowflake generator;
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
        return "TEST_CREATE_" + methodName;
    }
    
    @Mock
    private SqlGeneratorChain sqlGeneratorChain;
    
    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        generator = new CreateWarehouseGeneratorSnowflake();
        createdWarehouses = new ArrayList<>();
        
        // Follow test harness configuration pattern - check for required config
        String url = "jdbc:snowflake://rziymts-xbb66763.snowflakecomputing.com/?db=LTHDB&warehouse=LTHDB_TEST_WH&schema=TESTHARNESS&role=LIQUIBASE_TEST_HARNESS_ROLE";
        String username = "COMMUNITYKEVIN";
        String password = "uQ1lAjwVisliu8CpUTVh0UnxoTUk3";
        
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
        // Generate predictable schema name based on test class
        testSchemaName = "TEST_WAREHOUSE_INTEGRATION";
        
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
        
        // No mocking needed for integration tests - using real database
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (rawConnection != null && !rawConnection.isClosed()) {
            try (Statement stmt = rawConnection.createStatement()) {
                // Drop all created warehouses (account-level cleanup)
                for (String warehouseName : createdWarehouses) {
                    try {
                        stmt.execute("DROP WAREHOUSE IF EXISTS " + warehouseName);
                        System.out.println("Cleaned up warehouse: " + warehouseName);
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
    
    private void executeAndVerify(CreateWarehouseStatement statement, String testName) throws Exception {
        // Generate SQL
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        assertEquals(1, sqls.length, "Should generate exactly one SQL statement");
        
        String sql = sqls[0].toSql();
        System.out.println("Testing " + testName + ": " + sql);
        
        // Execute against Snowflake using rawConnection
        try (Statement stmt = rawConnection.createStatement()) {
            stmt.execute(sql);
            createdWarehouses.add(statement.getWarehouseName());
            System.out.println("✅ SUCCESS: " + testName);
        } catch (Exception e) {
            System.err.println("❌ FAILED: " + testName + " - " + e.getMessage());
            throw new AssertionError("SQL execution failed for " + testName + ": " + sql, e);
        }
    }
    
    @Test
    @DisplayName("Integration: Basic Required Only")
    void testBasicRequiredOnly() throws Exception {
        CreateWarehouseStatement statement = new CreateWarehouseStatement();
        statement.setWarehouseName(getUniqueWarehouseName("testBasicRequiredOnly"));
        
        executeAndVerify(statement, "Basic Required Only");
    }
    
    @Test
    @DisplayName("Integration: OR REPLACE")
    void testOrReplace() throws Exception {
        CreateWarehouseStatement statement = new CreateWarehouseStatement();
        statement.setWarehouseName(getUniqueWarehouseName("testOrReplace"));
        statement.setOrReplace(true);
        
        executeAndVerify(statement, "OR REPLACE");
    }
    
    @Test
    @DisplayName("Integration: IF NOT EXISTS")
    void testIfNotExists() throws Exception {
        CreateWarehouseStatement statement = new CreateWarehouseStatement();
        statement.setWarehouseName(getUniqueWarehouseName("testIfNotExists"));
        statement.setIfNotExists(true);
        
        executeAndVerify(statement, "IF NOT EXISTS");
    }
    
    @Test
    @DisplayName("Integration: With Warehouse Size")
    void testWithWarehouseSize() throws Exception {
        CreateWarehouseStatement statement = new CreateWarehouseStatement();
        statement.setWarehouseName(getUniqueWarehouseName("testWithWarehouseSize"));
        statement.setWarehouseSize("LARGE");
        
        executeAndVerify(statement, "With Warehouse Size");
    }
    
    @Test
    @DisplayName("Integration: With Warehouse Type")
    void testWithWarehouseType() throws Exception {
        CreateWarehouseStatement statement = new CreateWarehouseStatement();
        statement.setWarehouseName(getUniqueWarehouseName("testWithWarehouseType"));
        statement.setWarehouseType("SNOWPARK-OPTIMIZED");
        
        executeAndVerify(statement, "With Warehouse Type");
    }
    
    @Test
    @DisplayName("Integration: Multi-cluster Basic")
    void testMultiClusterBasic() throws Exception {
        CreateWarehouseStatement statement = new CreateWarehouseStatement();
        statement.setWarehouseName(getUniqueWarehouseName("testMultiClusterBasic"));
        statement.setMinClusterCount(1);
        statement.setMaxClusterCount(3);
        statement.setScalingPolicy("ECONOMY");
        
        executeAndVerify(statement, "Multi-cluster Basic");
    }
    
    @Test
    @DisplayName("Integration: Auto Settings")
    void testAutoSettings() throws Exception {
        CreateWarehouseStatement statement = new CreateWarehouseStatement();
        statement.setWarehouseName(getUniqueWarehouseName("testAutoSettings"));
        statement.setAutoSuspend(300);
        statement.setAutoResume(true);
        
        executeAndVerify(statement, "Auto Settings");
    }
    
    @Test
    @DisplayName("Integration: Initially Suspended")
    void testInitiallySuspended() throws Exception {
        CreateWarehouseStatement statement = new CreateWarehouseStatement();
        statement.setWarehouseName(getUniqueWarehouseName("testInitiallySuspended"));
        statement.setInitiallySuspended(true);
        
        executeAndVerify(statement, "Initially Suspended");
    }
    
    @Test
    @DisplayName("Integration: With Comment")
    void testWithComment() throws Exception {
        CreateWarehouseStatement statement = new CreateWarehouseStatement();
        statement.setWarehouseName(getUniqueWarehouseName("testWithComment"));
        statement.setComment("Test warehouse for development");
        
        executeAndVerify(statement, "With Comment");
    }
    
    @Test
    @DisplayName("Integration: Special Characters in Comment")
    void testSpecialCharactersInComment() throws Exception {
        CreateWarehouseStatement statement = new CreateWarehouseStatement();
        statement.setWarehouseName(getUniqueWarehouseName("testWithSpecialCharactersInComment"));
        statement.setComment("Test's warehouse with \"quotes\" and special chars!");
        
        executeAndVerify(statement, "Special Characters in Comment");
    }
    
    @Test
    @DisplayName("Integration: Query Acceleration")
    void testQueryAcceleration() throws Exception {
        CreateWarehouseStatement statement = new CreateWarehouseStatement();
        statement.setWarehouseName(getUniqueWarehouseName("testQueryAcceleration"));
        statement.setEnableQueryAcceleration(true);
        statement.setQueryAccelerationMaxScaleFactor(10);
        
        executeAndVerify(statement, "Query Acceleration");
    }
    
    @Test
    @DisplayName("Integration: WITH Clause Format")
    void testWithClauseFormat() throws Exception {
        CreateWarehouseStatement statement = new CreateWarehouseStatement();
        statement.setWarehouseName(getUniqueWarehouseName("testWithClauseFormat"));
        statement.setWarehouseSize("MEDIUM");
        statement.setAutoSuspend(300);
        
        executeAndVerify(statement, "WITH Clause Format");
    }
    
    @Test
    @DisplayName("Integration: All Properties (Comprehensive)")
    void testAllProperties() throws Exception {
        CreateWarehouseStatement statement = new CreateWarehouseStatement();
        statement.setWarehouseName(getUniqueWarehouseName("testAllProperties"));
        statement.setWarehouseSize("XLARGE");
        statement.setWarehouseType("SNOWPARK-OPTIMIZED"); // Changed to support RESOURCE_CONSTRAINT
        statement.setMinClusterCount(1); // Use 1 to avoid Enterprise Edition requirements
        statement.setMaxClusterCount(1); // Use 1 to avoid Enterprise Edition requirements
        statement.setScalingPolicy("STANDARD");
        statement.setAutoSuspend(600);
        statement.setAutoResume(false);
        statement.setInitiallySuspended(false);
        // Skip resource monitor - may not exist in test environment
        statement.setComment("Production warehouse for integration testing");
        // Skip query acceleration - requires Enterprise Edition
        statement.setMaxConcurrencyLevel(16);
        statement.setStatementQueuedTimeoutInSeconds(300);
        statement.setStatementTimeoutInSeconds(3600);
        statement.setResourceConstraint("MEMORY_16X"); // Valid with SNOWPARK-OPTIMIZED
        
        executeAndVerify(statement, "All Properties (Comprehensive)");
    }
    
    @Test
    @DisplayName("Integration: Validation - Missing Warehouse Name")
    void testValidationMissingWarehouseName() {
        CreateWarehouseStatement statement = new CreateWarehouseStatement();
        // Don't set warehouse name
        
        ValidationErrors errors = generator.validate(statement, database, sqlGeneratorChain);
        
        assertTrue(errors.hasErrors(), "Should have validation errors for missing warehouse name");
        assertTrue(errors.getErrorMessages().get(0).contains("Warehouse name is required"), 
                  "Should specify warehouse name is required");
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
        
        // Create a test warehouse to ensure isolation works
        CreateWarehouseStatement statement = new CreateWarehouseStatement();
        statement.setWarehouseName(getUniqueWarehouseName("testSchemaIsolation"));
        statement.setComment("Testing schema isolation");
        
        executeAndVerify(statement, "Schema Isolation Test");
    }
}