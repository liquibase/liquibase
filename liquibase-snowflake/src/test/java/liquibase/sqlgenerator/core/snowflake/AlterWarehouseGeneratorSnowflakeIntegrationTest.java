package liquibase.sqlgenerator.core.snowflake;

import liquibase.change.core.CreateWarehouseChange;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AlterWarehouseStatement;
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
 * Integration test for AlterWarehouseGeneratorSnowflake that tests all variations
 * against a live Snowflake database following test harness patterns.
 * 
 * Uses the same configuration as test harness: harness-config.local.yml
 * Implements schema isolation similar to SchemaIsolationHook for test safety.
 */
@DisplayName("AlterWarehouseGeneratorSnowflake Integration Tests")
public class AlterWarehouseGeneratorSnowflakeIntegrationTest {
    
    private AlterWarehouseGeneratorSnowflake generator;
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
        return "TEST_ALTER_" + methodName.toUpperCase() + "_" + System.currentTimeMillis();
    }
    
    @Mock
    private SqlGeneratorChain sqlGeneratorChain;
    
    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        generator = new AlterWarehouseGeneratorSnowflake();
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
        testSchemaName = "TEST_ALTER_WAREHOUSE_INTEGRATION";
        
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
                // Drop all created warehouses (account-level cleanup)
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
        // Use CreateWarehouseChange instead of raw SQL with OR REPLACE to avoid "already exists" errors
        CreateWarehouseChange createChange = new CreateWarehouseChange();
        createChange.setWarehouseName(warehouseName);
        createChange.setOrReplace(true); // Use CREATE OR REPLACE to avoid conflicts
        
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
    
    private void executeAndVerifyAlter(AlterWarehouseStatement statement, String testName) throws Exception {
        // Generate SQL
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        assertTrue(sqls.length >= 1 && sqls.length <= 2, "Should generate 1-2 SQL statements (ALTER + optional USE WAREHOUSE)");
        
        // Execute all generated SQL statements against Snowflake
        try (Statement stmt = rawConnection.createStatement()) {
            for (int i = 0; i < sqls.length; i++) {
                String sql = sqls[i].toSql();
                stmt.execute(sql);
            }
        } catch (Exception e) {
            System.err.println("❌ FAILED: " + testName + " - " + e.getMessage());
            String allSql = java.util.Arrays.stream(sqls).map(s -> s.toSql()).collect(java.util.stream.Collectors.joining("; "));
            throw new AssertionError("SQL execution failed for " + testName + ": " + allSql, e);
        }
    }
    
    @Test
    @DisplayName("Integration: RENAME TO")
    void testRename() throws Exception {
        String originalName = getUniqueWarehouseName("testRename_FROM");
        String newName = getUniqueWarehouseName("testRename_TO");
        
        // Create the warehouse to rename
        createTestWarehouse(originalName);
        
        AlterWarehouseStatement statement = new AlterWarehouseStatement();
        statement.setWarehouseName(originalName);
        statement.setOperationType(AlterWarehouseStatement.OperationType.RENAME);
        statement.setNewName(newName);
        
        executeAndVerifyAlter(statement, "RENAME TO");
        
        // Update our cleanup list
        createdWarehouses.remove(originalName);
        createdWarehouses.add(newName);
    }
    
    @Test
    @DisplayName("Integration: RENAME TO with IF EXISTS")
    void testRenameWithIfExists() throws Exception {
        String originalName = getUniqueWarehouseName("testRenameWithIfExists_FROM");
        String newName = getUniqueWarehouseName("testRenameWithIfExists_TO");
        
        // Create the warehouse to rename
        createTestWarehouse(originalName);
        
        AlterWarehouseStatement statement = new AlterWarehouseStatement();
        statement.setWarehouseName(originalName);
        statement.setOperationType(AlterWarehouseStatement.OperationType.RENAME);
        statement.setNewName(newName);
        statement.setIfExists(true);
        
        executeAndVerifyAlter(statement, "RENAME TO with IF EXISTS");
        
        // Update our cleanup list
        createdWarehouses.remove(originalName);
        createdWarehouses.add(newName);
    }
    
    @Test
    @DisplayName("Integration: SET WAREHOUSE_SIZE")
    void testSetWarehouseSize() throws Exception {
        String warehouseName = getUniqueWarehouseName("testSetWarehouseSize");
        
        // Create the warehouse to alter
        createTestWarehouse(warehouseName);
        
        AlterWarehouseStatement statement = new AlterWarehouseStatement();
        statement.setWarehouseName(warehouseName);
        statement.setOperationType(AlterWarehouseStatement.OperationType.SET);
        statement.setWarehouseSize("LARGE");
        
        executeAndVerifyAlter(statement, "SET WAREHOUSE_SIZE");
    }
    
    @Test
    @DisplayName("Integration: SET Multiple Properties")
    void testSetMultipleProperties() throws Exception {
        String warehouseName = getUniqueWarehouseName("testSetMultipleProperties");
        
        // Create the warehouse to alter
        createTestWarehouse(warehouseName);
        
        AlterWarehouseStatement statement = new AlterWarehouseStatement();
        statement.setWarehouseName(warehouseName);
        statement.setOperationType(AlterWarehouseStatement.OperationType.SET);
        statement.setWarehouseSize("MEDIUM");
        statement.setAutoSuspend(600);
        statement.setAutoResume(false);
        statement.setComment("Updated warehouse for testing");
        
        executeAndVerifyAlter(statement, "SET Multiple Properties");
    }
    
    @Test
    @DisplayName("Integration: SET Multi-cluster Properties")
    void testSetMultiClusterProperties() throws Exception {
        String warehouseName = getUniqueWarehouseName("testSetMultiClusterProperties");
        
        // Create the warehouse to alter
        createTestWarehouse(warehouseName);
        
        AlterWarehouseStatement statement = new AlterWarehouseStatement();
        statement.setWarehouseName(warehouseName);
        statement.setOperationType(AlterWarehouseStatement.OperationType.SET);
        statement.setMinClusterCount(1);
        statement.setMaxClusterCount(2);
        statement.setScalingPolicy("ECONOMY");
        
        executeAndVerifyAlter(statement, "SET Multi-cluster Properties");
    }
    
    @Test
    @DisplayName("Integration: UNSET Properties")
    void testUnsetProperties() throws Exception {
        String warehouseName = getUniqueWarehouseName("testUnsetProperties");
        
        // Create the warehouse with properties to unset using CreateWarehouseChange
        CreateWarehouseChange createChange = new CreateWarehouseChange();
        createChange.setWarehouseName(warehouseName);
        createChange.setAutoSuspend(300);
        createChange.setComment("Test comment");
        
        SqlStatement[] statements = createChange.generateStatements(database);
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
        
        AlterWarehouseStatement statement = new AlterWarehouseStatement();
        statement.setWarehouseName(warehouseName);
        statement.setOperationType(AlterWarehouseStatement.OperationType.UNSET);
        statement.setUnsetResourceMonitor(true);
        statement.setUnsetComment(true);
        
        executeAndVerifyAlter(statement, "UNSET Properties");
    }
    
    @Test
    @DisplayName("Integration: SUSPEND")
    void testSuspend() throws Exception {
        String warehouseName = getUniqueWarehouseName("testSuspend");
        
        // Create the warehouse (it will be running)
        createTestWarehouse(warehouseName);
        
        AlterWarehouseStatement statement = new AlterWarehouseStatement();
        statement.setWarehouseName(warehouseName);
        statement.setOperationType(AlterWarehouseStatement.OperationType.SUSPEND);
        
        executeAndVerifyAlter(statement, "SUSPEND");
    }
    
    @Test
    @DisplayName("Integration: RESUME")
    void testResume() throws Exception {
        String warehouseName = getUniqueWarehouseName("testResume");
        
        // Create and suspend the warehouse using CreateWarehouseChange
        CreateWarehouseChange createChange = new CreateWarehouseChange();
        createChange.setWarehouseName(warehouseName);
        createChange.setInitiallySuspended(true);
        
        SqlStatement[] statements = createChange.generateStatements(database);
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
        
        AlterWarehouseStatement statement = new AlterWarehouseStatement();
        statement.setWarehouseName(warehouseName);
        statement.setOperationType(AlterWarehouseStatement.OperationType.RESUME);
        
        executeAndVerifyAlter(statement, "RESUME");
    }
    
    @Test
    @DisplayName("Integration: RESUME IF SUSPENDED")
    void testResumeIfSuspended() throws Exception {
        String warehouseName = getUniqueWarehouseName("testResumeIfSuspended");
        
        // Create and suspend the warehouse using CreateWarehouseChange
        CreateWarehouseChange createChange = new CreateWarehouseChange();
        createChange.setWarehouseName(warehouseName);
        createChange.setInitiallySuspended(true);
        
        SqlStatement[] statements = createChange.generateStatements(database);
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
        
        AlterWarehouseStatement statement = new AlterWarehouseStatement();
        statement.setWarehouseName(warehouseName);
        statement.setOperationType(AlterWarehouseStatement.OperationType.RESUME);
        statement.setIfSuspended(true);
        
        executeAndVerifyAlter(statement, "RESUME IF SUSPENDED");
    }
    
    @Test
    @DisplayName("Integration: ABORT ALL QUERIES")
    void testAbortAllQueries() throws Exception {
        String warehouseName = getUniqueWarehouseName("testAbortAllQueries");
        
        // Create the warehouse
        createTestWarehouse(warehouseName);
        
        AlterWarehouseStatement statement = new AlterWarehouseStatement();
        statement.setWarehouseName(warehouseName);
        statement.setOperationType(AlterWarehouseStatement.OperationType.ABORT_ALL_QUERIES);
        
        executeAndVerifyAlter(statement, "ABORT ALL QUERIES");
    }
    
    @Test
    @DisplayName("Integration: Validation - Missing Warehouse Name")
    void testValidationMissingWarehouseName() {
        AlterWarehouseStatement statement = new AlterWarehouseStatement();
        statement.setOperationType(AlterWarehouseStatement.OperationType.SET);
        // Don't set warehouse name
        
        ValidationErrors errors = generator.validate(statement, database, sqlGeneratorChain);
        
        assertTrue(errors.hasErrors(), "Should have validation errors for missing warehouse name");
        assertTrue(errors.getErrorMessages().get(0).contains("Warehouse name is required"), "Assertion should be true");    }
    
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
        
        // Create and alter a test warehouse to ensure isolation works
        String warehouseName = getUniqueWarehouseName("testSchemaIsolation");
        createTestWarehouse(warehouseName);
        
        AlterWarehouseStatement statement = new AlterWarehouseStatement();
        statement.setWarehouseName(warehouseName);
        statement.setOperationType(AlterWarehouseStatement.OperationType.SET);
        statement.setComment("Testing schema isolation for ALTER WAREHOUSE");
        
        executeAndVerifyAlter(statement, "Schema Isolation Test");
    }
}