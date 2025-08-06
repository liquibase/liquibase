package liquibase.integration;

import liquibase.util.TestDatabaseConfigUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Investigation test to determine what columns actually exist in INFORMATION_SCHEMA.FILE_FORMATS
 * This helps us verify which properties from the requirements document are actually available.
 */
public class FileFormatSchemaInvestigationTest {

    private Connection connection;

    @BeforeEach
    public void setUp() throws Exception {
        connection = TestDatabaseConfigUtil.getSnowflakeConnection();
    }

    @Test
    public void investigateAvailableColumns() throws Exception {
        System.out.println("=== INVESTIGATING INFORMATION_SCHEMA.FILE_FORMATS COLUMNS ===");
        
        // Query to get all column information
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE " +
                "FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_SCHEMA = 'INFORMATION_SCHEMA' " +
                "AND TABLE_NAME = 'FILE_FORMATS' " +
                "ORDER BY ORDINAL_POSITION"
            );
            
            System.out.println("Available columns in INFORMATION_SCHEMA.FILE_FORMATS:");
            System.out.println("Column Name | Data Type | Nullable");
            System.out.println("------------|-----------|----------");
            
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                String dataType = rs.getString("DATA_TYPE");
                String nullable = rs.getString("IS_NULLABLE");
                System.out.println(String.format("%-30s | %-15s | %s", columnName, dataType, nullable));
            }
            rs.close();
        }
        
        System.out.println("\n=== TESTING SAMPLE QUERY ===");
        
        // Test if we can query with a minimal set of columns
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT FILE_FORMAT_NAME, FILE_FORMAT_TYPE " +
                "FROM INFORMATION_SCHEMA.FILE_FORMATS " +
                "LIMIT 1"
            );
            
            if (rs.next()) {
                System.out.println("Sample file format found: " + rs.getString("FILE_FORMAT_NAME") + 
                                   " (Type: " + rs.getString("FILE_FORMAT_TYPE") + ")");
            } else {
                System.out.println("No file formats found in current schema");
            }
            rs.close();
        }
    }
}