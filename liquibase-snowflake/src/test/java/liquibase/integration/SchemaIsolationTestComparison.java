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
 * Test to verify schema isolation capability works with the YAML configuration pattern.
 * Uses TestDatabaseConfigUtil to read from liquibase.sdk.local.yaml.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SchemaIsolationTestComparison {

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
    public void testNewPatternSchemaIsolation() throws Exception {
        String testSchemaName = "NEW_PATTERN_SCHEMA_" + StringUtil.randomIdentifier(8).toUpperCase();
        String testTableName = "TEST_TABLE_" + StringUtil.randomIdentifier(8).toUpperCase();
        
        try {
            // Test 1: Create schema and switch to it
            executeSql(String.format("CREATE SCHEMA %s", testSchemaName));
            executeSql(String.format("USE SCHEMA %s", testSchemaName));
            
            // Test 2: Create table in isolated schema
            executeSql(String.format("CREATE TABLE %s (id INT, data VARCHAR(100))", testTableName));
            executeSql(String.format("INSERT INTO %s VALUES (1, 'test_data')", testTableName));
            
            // Test 3: Verify schema isolation - table should exist in our schema
            PreparedStatement stmt = connection.prepareStatement(
                String.format("SELECT COUNT(*) FROM %s", testTableName));
            ResultSet rs = stmt.executeQuery();
            
            assertTrue(rs.next(), "Should find data in isolated schema");
            assertEquals(1, rs.getInt(1), "Should have one row in isolated schema");
            
            rs.close();
            stmt.close();
            
            // Test 4: Switch back to base schema - table should not be visible
            executeSql("USE SCHEMA BASE_SCHEMA");
            
            try {
                PreparedStatement baseStmt = connection.prepareStatement(
                    String.format("SELECT COUNT(*) FROM %s", testTableName));
                baseStmt.executeQuery();
                fail("Table should not be visible in BASE_SCHEMA");
            } catch (Exception e) {
                // Expected - table doesn't exist in BASE_SCHEMA
                assertTrue(e.getMessage().contains("does not exist") || 
                          e.getMessage().contains("not found"), 
                          "Should get 'not found' error");
            }
            
            // Test 5: Access with fully qualified name should work
            PreparedStatement qualifiedStmt = connection.prepareStatement(
                String.format("SELECT COUNT(*) FROM %s.%s", testSchemaName, testTableName));
            ResultSet qualifiedRs = qualifiedStmt.executeQuery();
            
            assertTrue(qualifiedRs.next(), "Should access table via qualified name");
            assertEquals(1, qualifiedRs.getInt(1), "Should have one row via qualified access");
            
            qualifiedRs.close();
            qualifiedStmt.close();
            
        } finally {
            // Cleanup
            try {
                executeSql(String.format("DROP SCHEMA IF EXISTS %s CASCADE", testSchemaName));
                executeSql("USE SCHEMA BASE_SCHEMA"); // Ensure we're back to default
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
    }

    @Test 
    public void testSchemaConfigurationAccess() throws Exception {
        // Test that we can access configured schema values
        
        assertNotNull(database, "Database should be available");
        
        // Test default schema access
        String defaultSchema = database.getDefaultSchemaName();
        assertEquals("BASE_SCHEMA", defaultSchema, "Default schema should match config");
        
        // Test alt schema access via YAML configuration
        String altSchema = TestDatabaseConfigUtil.getSnowflakeAltSchema();
        assertEquals("ALT_SCHEMA", altSchema, "Alt schema should match config");
        
        // Test that we can create objects in alt schema
        String testTable = "ALT_SCHEMA_TEST_" + StringUtil.randomIdentifier(6).toUpperCase();
        
        try {
            // Create table in alt schema
            executeSql(String.format("CREATE TABLE %s.%s (test_col INT)", altSchema, testTable));
            
            // Verify it exists in alt schema
            PreparedStatement stmt = connection.prepareStatement(
                String.format("SELECT COUNT(*) FROM %s.%s", altSchema, testTable));
            ResultSet rs = stmt.executeQuery();
            
            assertTrue(rs.next(), "Should access table in alt schema");
            assertEquals(0, rs.getInt(1), "New table should be empty");
            
            rs.close();
            stmt.close();
            
        } finally {
            // Cleanup
            try {
                executeSql(String.format("DROP TABLE IF EXISTS %s.%s", altSchema, testTable));
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
    }

    @Test
    public void testYamlConfigurationEquivalency() throws Exception {
        // Verify that YAML configuration provides same connection as direct approach
        String url = TestDatabaseConfigUtil.getSnowflakeUrl();
        String username = TestDatabaseConfigUtil.getSnowflakeUsername();
        String password = TestDatabaseConfigUtil.getSnowflakePassword();
        
        // Direct connection using YAML values
        Connection directConn = connection; // Already using YAML config
        Database directDatabase = database; // Already using YAML config
        
        // Alternative connection using YAML utility methods
        Connection yamlConn = TestDatabaseConfigUtil.getSnowflakeConnection();
        Database yamlDatabase = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(yamlConn));
        
        try {
            // Both should point to same database
            assertEquals(directDatabase.getDefaultCatalogName(), 
                        yamlDatabase.getDefaultCatalogName(),
                        "Catalogs should match");
            
            assertEquals(directDatabase.getDefaultSchemaName(),
                        yamlDatabase.getDefaultSchemaName(), 
                        "Default schemas should match");
            
            // Both should be able to create tables
            String testTable = "YAML_COMPARISON_" + StringUtil.randomIdentifier(6).toUpperCase();
            
            // Create via main connection
            executeSql(String.format("CREATE TABLE %s (id INT)", testTable));
            
            // Query via YAML connection
            PreparedStatement yamlStmt = yamlConn.prepareStatement(
                String.format("SELECT COUNT(*) FROM %s", testTable));
            ResultSet rs = yamlStmt.executeQuery();
            
            assertTrue(rs.next(), "YAML connection should see table created by main connection");
            assertEquals(0, rs.getInt(1), "Table should be empty");
            
            rs.close();
            yamlStmt.close();
            
            // Cleanup via main connection
            executeSql(String.format("DROP TABLE %s", testTable));
            
        } finally {
            yamlConn.close();
        }
    }
}