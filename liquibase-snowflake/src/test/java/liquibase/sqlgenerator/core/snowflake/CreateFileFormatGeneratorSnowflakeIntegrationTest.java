package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CreateFileFormatStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Assumptions;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for CreateFileFormatGeneratorSnowflake.
 * These tests require a live Snowflake connection.
 */
@DisplayName("CreateFileFormatGeneratorSnowflake Integration Tests")
public class CreateFileFormatGeneratorSnowflakeIntegrationTest {
    
    private CreateFileFormatGeneratorSnowflake generator;
    private Database database;
    private Connection connection;
    private SqlGeneratorChain sqlGeneratorChain;
    
    @BeforeEach
    void setUp() throws Exception {
        String url = System.getProperty("SNOWFLAKE_URL", System.getenv("SNOWFLAKE_URL"));
        String user = System.getProperty("SNOWFLAKE_USER", System.getenv("SNOWFLAKE_USER"));
        String password = System.getProperty("SNOWFLAKE_PASSWORD", System.getenv("SNOWFLAKE_PASSWORD"));
        
        // Skip integration tests if Snowflake credentials not available
        Assumptions.assumeTrue(url != null && user != null && password != null,
            "Snowflake credentials not available - skipping integration tests");
        
        try {
            Class.forName("net.snowflake.client.jdbc.SnowflakeDriver");
            connection = DriverManager.getConnection(url, user, password);
            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            generator = new CreateFileFormatGeneratorSnowflake();
            sqlGeneratorChain = null; // Not needed for integration tests
            
            // Clean up any existing test file formats
            cleanupTestFileFormats();
            
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "Failed to connect to Snowflake: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Should create basic CSV file format in Snowflake")
    void shouldCreateBasicCsvFileFormat() throws Exception {
        String formatName = "TEST_CSV_FORMAT_" + System.currentTimeMillis();
        
        CreateFileFormatStatement statement = new CreateFileFormatStatement();
        statement.setFileFormatName(formatName);
        statement.setFileFormatType("CSV");
        statement.setFieldDelimiter(",");
        statement.setSkipHeader(1);
        statement.setTrimSpace(true);
        statement.setComment("Test CSV format created by integration test");
        
        // Generate SQL
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        assertEquals(1, sqls.length);
        
        String sql = sqls[0].toSql();
        System.out.println("Generated SQL: " + sql);
        
        // Execute the SQL
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
        
        // Verify the file format was created
        verifyFileFormatExists(formatName, "CSV");
        
        // Clean up
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP FILE FORMAT IF EXISTS " + formatName);
        }
    }
    
    @Test
    @DisplayName("Should create JSON file format with options in Snowflake")
    void shouldCreateJsonFileFormatWithOptions() throws Exception {
        String formatName = "TEST_JSON_FORMAT_" + System.currentTimeMillis();
        
        CreateFileFormatStatement statement = new CreateFileFormatStatement();
        statement.setFileFormatName(formatName);
        statement.setFileFormatType("JSON");
        statement.setCompression("GZIP");
        statement.setStripOuterArray(true);
        statement.setComment("Test JSON format with options");
        
        // Generate and execute SQL
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        String sql = sqls[0].toSql();
        System.out.println("Generated SQL: " + sql);
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
        
        // Verify the file format was created with correct options
        verifyFileFormatExists(formatName, "JSON");
        verifyFileFormatOption(formatName, "COMPRESSION", "GZIP");
        // Note: STRIP_OUTER_ARRAY not available in INFORMATION_SCHEMA.FILE_FORMATS
        
        // Clean up
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP FILE FORMAT IF EXISTS " + formatName);
        }
    }
    
    @Test
    @DisplayName("Should create file format with OR REPLACE")
    void shouldCreateFileFormatWithOrReplace() throws Exception {
        String formatName = "TEST_REPLACE_FORMAT_" + System.currentTimeMillis();
        
        // First create a basic file format
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE FILE FORMAT " + formatName + " TYPE = CSV");
        }
        
        // Now create with OR REPLACE
        CreateFileFormatStatement statement = new CreateFileFormatStatement();
        statement.setFileFormatName(formatName);
        statement.setOrReplace(true);
        statement.setFileFormatType("JSON");
        statement.setComment("Replaced format");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        String sql = sqls[0].toSql();
        System.out.println("Generated OR REPLACE SQL: " + sql);
        
        assertTrue(sql.contains("CREATE OR REPLACE"));
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
        
        // Verify it's now a JSON format (was replaced)
        verifyFileFormatExists(formatName, "JSON");
        
        // Clean up
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP FILE FORMAT IF EXISTS " + formatName);
        }
    }
    
    @Test
    @DisplayName("Should handle IF NOT EXISTS correctly")
    void shouldHandleIfNotExistsCorrectly() throws Exception {
        String formatName = "TEST_IF_NOT_EXISTS_" + System.currentTimeMillis();
        
        CreateFileFormatStatement statement = new CreateFileFormatStatement();
        statement.setFileFormatName(formatName);
        statement.setIfNotExists(true);
        statement.setFileFormatType("PARQUET");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        String sql = sqls[0].toSql();
        System.out.println("Generated IF NOT EXISTS SQL: " + sql);
        
        assertTrue(sql.contains("IF NOT EXISTS"));
        
        // Execute twice - second should not fail due to IF NOT EXISTS
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            stmt.execute(sql); // Should not fail
        }
        
        verifyFileFormatExists(formatName, "PARQUET");
        
        // Clean up
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP FILE FORMAT IF EXISTS " + formatName);
        }
    }
    
    private void verifyFileFormatExists(String formatName, String expectedType) throws Exception {
        String query = "SELECT FILE_FORMAT_TYPE FROM INFORMATION_SCHEMA.FILE_FORMATS WHERE FILE_FORMAT_NAME = ?";
        try (java.sql.PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, formatName);
            ResultSet rs = stmt.executeQuery();
            
            assertTrue(rs.next(), "File format " + formatName + " should exist");
            assertEquals(expectedType, rs.getString("FILE_FORMAT_TYPE"));
        }
    }
    
    private void verifyFileFormatOption(String formatName, String optionName, String expectedValue) throws Exception {
        String query = "SELECT " + optionName + " FROM INFORMATION_SCHEMA.FILE_FORMATS WHERE FILE_FORMAT_NAME = ?";
        try (java.sql.PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, formatName);
            ResultSet rs = stmt.executeQuery();
            
            assertTrue(rs.next(), "File format " + formatName + " should exist");
            String actualValue = rs.getString(optionName);
            assertEquals(expectedValue, actualValue, "Option " + optionName + " should match expected value");
        }
    }
    
    private void cleanupTestFileFormats() throws Exception {
        String query = "SHOW FILE FORMATS LIKE 'TEST_%_FORMAT_%'";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String formatName = rs.getString("name");
                try {
                    stmt.execute("DROP FILE FORMAT IF EXISTS " + formatName);
                } catch (Exception e) {
                    // Ignore cleanup errors
                    System.out.println("Warning: Could not clean up " + formatName + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            // Ignore cleanup errors
            System.out.println("Warning: Could not perform cleanup: " + e.getMessage());
        }
    }
}