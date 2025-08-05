package liquibase.integration;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.util.StringUtil;
import liquibase.util.TestDatabaseConfigUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Modern integration test using YAML configuration.
 * Uses TestDatabaseConfigUtil to read from src/test/resources/liquibase.sdk.local.yaml
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ModernSnowflakeIntegrationTest {

    private Database database;
    private Connection connection;

    @BeforeAll
    public void setUp() throws Exception {
        // Use YAML configuration instead of TestSystemFactory
        connection = TestDatabaseConfigUtil.getSnowflakeConnection();
        database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
    }

    /**
     * Helper method to execute SQL statements
     */
    private void executeSql(String sql) throws Exception {
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.execute();
        stmt.close();
    }

    @Test
    public void testDatabaseConnectionWorks() throws Exception {
        // Simple test to verify the configuration works
        assertNotNull(database, "Database should be initialized");
        assertTrue(database.getConnection().getURL().contains("snowflake"), 
                   "Should be connected to Snowflake");
    }

    @Test
    public void testTableCreationAndQuery() throws Exception {
        String tableName = "MODERN_TEST_TABLE_" + StringUtil.randomIdentifier(8).toUpperCase();
        
        try {
            // Create test table
            executeSql(String.format(
                "CREATE TABLE %s (id INTEGER, name VARCHAR(50))", tableName));
            
            // Insert test data
            executeSql(String.format(
                "INSERT INTO %s (id, name) VALUES (1, 'test')", tableName));
            
            // Verify table exists and has data
            PreparedStatement stmt = connection.prepareStatement(
                String.format("SELECT COUNT(*) FROM %s", tableName));
            ResultSet rs = stmt.executeQuery();
            
            assertTrue(rs.next(), "Should have at least one row");
            assertEquals(1, rs.getInt(1), "Should have exactly one row");
            
            rs.close();
            stmt.close();
            
        } finally {
            // Cleanup
            try {
                executeSql(String.format("DROP TABLE IF EXISTS %s", tableName));
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
    }

    @Test
    public void testSchemaOperations() throws Exception {
        String schemaName = "MODERN_TEST_SCHEMA_" + StringUtil.randomIdentifier(8).toUpperCase();
        
        try {
            // Create test schema
            executeSql(String.format("CREATE SCHEMA %s", schemaName));
            
            // Use the schema
            executeSql(String.format("USE SCHEMA %s", schemaName));
            
            // Create table in the schema
            executeSql("CREATE TABLE test_table_in_schema (id INT)");
            
            // Verify we can query the table in the schema
            PreparedStatement stmt = connection.prepareStatement(
                "SELECT COUNT(*) FROM test_table_in_schema");
            ResultSet rs = stmt.executeQuery();
            
            assertTrue(rs.next(), "Should be able to query table in schema");
            assertEquals(0, rs.getInt(1), "New table should be empty");
            
            rs.close();
            stmt.close();
            
        } finally {
            // Cleanup
            try {
                executeSql(String.format("DROP SCHEMA IF EXISTS %s CASCADE", schemaName));
                executeSql("USE SCHEMA BASE_SCHEMA"); // Return to default schema
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
    }
}