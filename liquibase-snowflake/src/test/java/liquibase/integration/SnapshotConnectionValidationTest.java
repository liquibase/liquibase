package liquibase.integration;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Connection validation test for snapshot/diff integration testing.
 * Validates that the LB_INT_SNAPSHOT_DB environment is accessible and functional.
 * 
 * ADDRESSES_CORE_ISSUE: Validate test environment before integration tests.
 */
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

    @BeforeEach
    public void setUp() throws Exception {
        String url = System.getenv("SNOWFLAKE_URL");
        String user = System.getenv("SNOWFLAKE_USER");
        String password = System.getenv("SNOWFLAKE_PASSWORD");
        
        if (url == null || user == null || password == null) {
            throw new RuntimeException("Snowflake connection environment variables not set");
        }

        // For integration tests, we expect the URL to point to LB_INT_SNAPSHOT_DB
        if (!url.contains("LB_INT_SNAPSHOT_DB")) {
            System.out.println("WARNING: URL should contain LB_INT_SNAPSHOT_DB for integration tests");
            System.out.println("Current URL: " + url);
        }

        connection = DriverManager.getConnection(url, user, password);
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
                System.out.println("Cleaned up test object: " + objectName);
            } catch (Exception e) {
                System.err.println("Failed to cleanup test object " + objectName + ": " + e.getMessage());
            }
        }
        
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    public void testSnapshotEnvironmentConnection() throws Exception {
        System.out.println("Testing connection to snapshot integration test environment...");
        
        // Verify connection is active
        assertNotNull(connection, "Connection should be established");
        assertFalse(connection.isClosed(), "Connection should be active");
        
        // Verify we can query the database
        PreparedStatement stmt = connection.prepareStatement("SELECT CURRENT_DATABASE(), CURRENT_SCHEMA(), CURRENT_ROLE()");
        ResultSet rs = stmt.executeQuery();
        
        assertTrue(rs.next(), "Should be able to query current context");
        
        String currentDatabase = rs.getString(1);
        String currentSchema = rs.getString(2);
        String currentRole = rs.getString(3);
        
        System.out.println("✅ Connected to database: " + currentDatabase);
        System.out.println("✅ Using schema: " + currentSchema);
        System.out.println("✅ With role: " + currentRole);
        
        // Verify expected test environment
        assertEquals("LB_INT_SNAPSHOT_DB", currentDatabase, "Should be connected to integration test database");
        assertEquals("BASE_SCHEMA", currentSchema, "Should be using integration test schema");
        assertEquals("LB_INT_ROLE", currentRole, "Should be using integration test role");
        
        rs.close();
        stmt.close();
        
        System.out.println("✅ SUCCESS: Snapshot integration test environment validated");
    }

    @Test
    public void testSchemaIsolationCapability() throws Exception {
        System.out.println("Testing schema isolation for parallel test execution...");
        
        String testTableName = getUniqueTestObjectName("schema_isolation");
        createdTestObjects.add(testTableName);
        
        // Create a test table to verify we can create objects
        PreparedStatement createStmt = connection.prepareStatement(
            "CREATE TABLE " + testTableName + " (id INTEGER, name VARCHAR(50))");
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
        
        System.out.println("✅ SUCCESS: Schema isolation working - can create unique test objects");
    }

    @Test
    public void testInformationSchemaAccess() throws Exception {
        System.out.println("Testing INFORMATION_SCHEMA access for snapshot queries...");
        
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
            
            System.out.println("✅ Can access: " + view);
        }
        
        System.out.println("✅ SUCCESS: All required INFORMATION_SCHEMA views accessible");
    }

    @Test
    public void testShowCommandsAccess() throws Exception {
        System.out.println("Testing SHOW commands access for supplementary snapshot queries...");
        
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
            
            System.out.println("✅ Can execute: " + showCommand);
        }
        
        System.out.println("✅ SUCCESS: All required SHOW commands accessible");
    }

    @Test
    public void testDatabaseFactory() throws Exception {
        System.out.println("Testing Liquibase Database factory integration...");
        
        // Verify we get a Snowflake database instance
        assertNotNull(database, "Database instance should be created");
        assertTrue(database instanceof liquibase.database.core.SnowflakeDatabase, 
                  "Should be a SnowflakeDatabase instance");
        
        // Verify basic database properties
        assertEquals("Snowflake", database.getDatabaseProductName(), "Product name should be Snowflake");
        assertNotNull(database.getConnection(), "Database should have connection");
        
        System.out.println("✅ Database type: " + database.getClass().getSimpleName());
        System.out.println("✅ Product name: " + database.getDatabaseProductName());
        System.out.println("✅ Connection active: " + !database.getConnection().isClosed());
        
        System.out.println("✅ SUCCESS: Liquibase Database factory working correctly");
    }
}