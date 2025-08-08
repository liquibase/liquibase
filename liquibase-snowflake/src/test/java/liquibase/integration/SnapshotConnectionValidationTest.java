package liquibase.integration;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.util.TestDatabaseConfigUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Connection validation test for snapshot/diff integration testing.
 * Uses configuration from src/test/resources/liquibase.sdk.local.yaml instead of environment variables.
 * This eliminates the need to set SNOWFLAKE_URL, SNOWFLAKE_USER, SNOWFLAKE_PASSWORD environment variables.
 * 
 * ADDRESSES_CORE_ISSUE: Validate test environment before integration tests.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SnapshotConnectionValidationTest {

    private Database database;
    private Connection connection;
    private List<String> createdTestObjects = new ArrayList<>();

    /**
     * CRITICAL: Generates unique test object names for schema isolation.
     * ADDRESSES_CORE_ISSUE: Schema-level object naming conflicts preventing parallel execution.
     * 
     * @param methodName The test method name
     * @return Unique test object name for parallel execution
     */
    private String getUniqueTestObjectName(String methodName) {
        return "SNAP_TEST_" + methodName.toUpperCase() + "_" + System.currentTimeMillis();
    }

    @BeforeAll
    public void setUp() throws Exception {
        // Get database connection using configuration from liquibase.sdk.local.yaml
        // No environment variables needed!
        connection = TestDatabaseConfigUtil.getSnowflakeConnection();
        database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
    }

    @AfterEach
    public void tearDown() throws Exception {
        // MANDATORY: Cleanup all created test objects using unique names
        for (String objectName : createdTestObjects) {
            try {
                // Clean up various object types that might be created
                PreparedStatement dropStmt = connection.prepareStatement("DROP TABLE IF EXISTS " + objectName);
                dropStmt.execute();
                dropStmt.close();
            } catch (Exception e) {
                System.err.println("Failed to cleanup test object " + objectName + ": " + e.getMessage());
            }
        }
        createdTestObjects.clear();
        
        // Note: Connection is closed when JVM shuts down in @BeforeAll pattern
    }

    @Test
    public void testSnapshotEnvironmentConnection() throws Exception {
        
        // Verify database and connection are initialized
        assertNotNull(database, "Database should be established");
        assertNotNull(connection, "Connection should be established");
        assertFalse(connection.isClosed(), "Connection should be active");
        
        // Verify we can query the database
        PreparedStatement stmt = connection.prepareStatement("SELECT CURRENT_DATABASE(), CURRENT_SCHEMA(), CURRENT_ROLE()");
        ResultSet rs = stmt.executeQuery();
        
        assertTrue(rs.next(), "Should be able to query current context");
        
        String currentDatabase = rs.getString(1);
        String currentSchema = rs.getString(2);
        String currentRole = rs.getString(3);
        
        
        // Verify expected test environment (updated for current configuration)
        assertEquals("LB_DBEXT_INT_DB", currentDatabase, "Should be connected to integration test database");
        assertEquals("BASE_SCHEMA", currentSchema, "Should be using integration test schema");
        assertEquals("LB_INT_ROLE", currentRole, "Should be using integration test role");
        
        rs.close();
        stmt.close();
        
    }

    @Test
    public void testSchemaIsolationCapability() throws Exception {
        
        String testTableName = getUniqueTestObjectName("schema_isolation");
        createdTestObjects.add(testTableName);
        
        // Create a test table to verify we can create objects
        PreparedStatement createStmt = connection.prepareStatement(
            String.format("CREATE TABLE %s (id INTEGER, name VARCHAR(50))", testTableName));
        createStmt.execute();
        createStmt.close();
        
        // Verify the table was created
        PreparedStatement queryStmt = connection.prepareStatement(
            "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?");
        queryStmt.setString(1, testTableName);
        ResultSet rs = queryStmt.executeQuery();
        
        assertTrue(rs.next(), "Test table should be created successfully");
        assertEquals(testTableName, rs.getString("TABLE_NAME"), "Table name should match");
        
        rs.close();
        queryStmt.close();
        
    }

    @Test
    public void testInformationSchemaAccess() throws Exception {
        
        // Test access to key INFORMATION_SCHEMA views used by snapshot generators
        String[] requiredViews = {
            "INFORMATION_SCHEMA.DATABASES",
            "INFORMATION_SCHEMA.SCHEMATA", 
            "INFORMATION_SCHEMA.TABLES",
            "INFORMATION_SCHEMA.SEQUENCES"
        };
        
        for (String view : requiredViews) {
            PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM " + view + " LIMIT 1");
            ResultSet rs = stmt.executeQuery();
            
            assertTrue(rs.next(), "Should be able to query " + view);
            // Don't assert on count value since it might be 0 in fresh environment
            
            rs.close();
            stmt.close();
            
        }
        
    }

    @Test
    public void testShowCommandsAccess() throws Exception {
        
        // Test access to SHOW commands used by snapshot generators
        String[] showCommands = {
            "SHOW DATABASES",
            "SHOW SCHEMAS", 
            "SHOW TABLES",
            "SHOW SEQUENCES"
        };
        
        for (String showCommand : showCommands) {
            PreparedStatement stmt = connection.prepareStatement(showCommand);
            ResultSet rs = stmt.executeQuery();
            
            // Don't need to check for results, just that the command executes
            assertNotNull(rs, "Should be able to execute " + showCommand);
            
            rs.close();
            stmt.close();
            
        }
        
    }

    @Test
    public void testDatabaseFactory() throws Exception {
        
        // Verify we get a Snowflake database instance
        assertNotNull(database, "Database instance should be created");
        assertTrue(database instanceof liquibase.database.core.SnowflakeDatabase, "Assertion should be true");        
        // Verify basic database properties
        assertEquals("Snowflake", database.getDatabaseProductName(), "Product name should be Snowflake");
        assertNotNull(database.getConnection(), "Database should have connection");
        
        
    }
}