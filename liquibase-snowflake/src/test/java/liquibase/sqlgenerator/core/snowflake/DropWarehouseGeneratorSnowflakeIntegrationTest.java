package liquibase.sqlgenerator.core.snowflake;

import liquibase.change.core.CreateWarehouseChange;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.snowflake.DropWarehouseStatement;
import liquibase.util.TestDatabaseConfigUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
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
        return "TEST_DROP_" + methodName.toUpperCase() + "_" + System.currentTimeMillis();
    }
    
    @Mock
    private SqlGeneratorChain sqlGeneratorChain;
    
    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        generator = new DropWarehouseGeneratorSnowflake();
        createdWarehouses = new ArrayList<>();
        
        // Initialize database connection using YAML configuration
        try {
            rawConnection = TestDatabaseConfigUtil.getSnowflakeConnection();
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
            try (Statement stmt = rawConnection.createStatement()) {
                stmt.execute("CREATE SCHEMA IF NOT EXISTS " + testSchemaName);
                stmt.execute("USE SCHEMA " + testSchemaName);
            }
            
            // Update database to use isolated schema (like SchemaIsolationHook does)
            database.setDefaultSchemaName(testSchemaName);
            
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
                    } catch (Exception e) {
                        System.err.println("Failed to cleanup warehouse " + warehouseName + ": " + e.getMessage());
                    }
                }
                
                // Switch back to original schema before dropping test schema (like SchemaIsolationHook does)
                if (originalSchema != null) {
                    try {
                        stmt.execute("USE SCHEMA " + originalSchema);
                    } catch (Exception e) {
                        System.err.println("Failed to switch back to original schema: " + e.getMessage());
                    }
                }
                
                // Drop test schema (like SchemaIsolationHook cleanup)
                try {
                    stmt.execute("DROP SCHEMA IF EXISTS " + testSchemaName);
                } catch (Exception e) {
                    System.err.println("Failed to cleanup schema " + testSchemaName + ": " + e.getMessage());
                }
            }
            rawConnection.close();
        }
    }
    
    private void createTestWarehouse(String warehouseName) throws Exception {
        // Use CreateWarehouseChange instead of raw SQL
        CreateWarehouseChange createChange = new CreateWarehouseChange();
        createChange.setWarehouseName(warehouseName);
        
        // Generate and execute SQL using our changetype
        SqlStatement[] statements = createChange.generateStatements(database);
        assertTrue(statements.length > 0, "CreateWarehouseChange should generate SQL statements");
        
        try (Statement stmt = rawConnection.createStatement()) {
            for (SqlStatement statement : statements) {
                // Use SQL generator to get actual SQL string
                Sql[] sqls = liquibase.sqlgenerator.SqlGeneratorFactory.getInstance().generateSql(statement, database);
                for (Sql sql : sqls) {
                    String sqlString = sql.toSql();
                    stmt.execute(sqlString);
                }
            }
            createdWarehouses.add(warehouseName);
        }
    }
    
    private void executeAndVerifyDrop(DropWarehouseStatement statement, String testName) throws Exception {
        // Generate SQL
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        assertEquals(1, sqls.length, "Should generate exactly one SQL statement");
        
        String sql = sqls[0].toSql();
        
        // Execute against Snowflake using rawConnection
        try (Statement stmt = rawConnection.createStatement()) {
            stmt.execute(sql);
            // Remove from our cleanup list since it's now dropped
            createdWarehouses.remove(statement.getWarehouseName());
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
        
        // Create and ensure the warehouse is running using CreateWarehouseChange
        CreateWarehouseChange createChange = new CreateWarehouseChange();
        createChange.setWarehouseName(warehouseName);
        createChange.setInitiallySuspended(false);
        
        SqlStatement[] statements = createChange.generateStatements(database);
        try (Statement stmt = rawConnection.createStatement()) {
            for (SqlStatement statement : statements) {
                Sql[] sqls = liquibase.sqlgenerator.SqlGeneratorFactory.getInstance().generateSql(statement, database);
                for (Sql sql : sqls) {
                    String sqlString = sql.toSql();
                    stmt.execute(sqlString);
                }
            }
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
        
        // Create and ensure the warehouse is suspended using CreateWarehouseChange
        CreateWarehouseChange createChange = new CreateWarehouseChange();
        createChange.setWarehouseName(warehouseName);
        createChange.setInitiallySuspended(true);
        
        SqlStatement[] statements = createChange.generateStatements(database);
        try (Statement stmt = rawConnection.createStatement()) {
            for (SqlStatement statement : statements) {
                Sql[] sqls = liquibase.sqlgenerator.SqlGeneratorFactory.getInstance().generateSql(statement, database);
                for (Sql sql : sqls) {
                    String sqlString = sql.toSql();
                    stmt.execute(sqlString);
                }
            }
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
        
        // Create warehouse with various properties using CreateWarehouseChange
        CreateWarehouseChange createChange = new CreateWarehouseChange();
        createChange.setWarehouseName(warehouseName);
        createChange.setWarehouseSize("LARGE");
        createChange.setAutoSuspend(300);
        createChange.setAutoResume(true);
        createChange.setComment("Test warehouse with properties");
        
        SqlStatement[] statements = createChange.generateStatements(database);
        try (Statement stmt = rawConnection.createStatement()) {
            for (SqlStatement statement : statements) {
                Sql[] sqls = liquibase.sqlgenerator.SqlGeneratorFactory.getInstance().generateSql(statement, database);
                for (Sql sql : sqls) {
                    String sqlString = sql.toSql();
                    stmt.execute(sqlString);
                }
            }
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
        assertTrue(errors.getErrorMessages().get(0).contains("Warehouse name is required"), "Assertion should be true");    }
    
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
        
        // This should fail with an error
        try (Statement stmt = rawConnection.createStatement()) {
            Exception exception = assertThrows(Exception.class, () -> {
                stmt.execute(sql);
            });
            assertTrue(exception.getMessage().toLowerCase().contains("does not exist") || 
                      exception.getMessage().toLowerCase().contains("not found"),
                      "Exception should mention non-existent warehouse");
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