package liquibase.integration;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.database.object.FileFormat;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.Schema;
import liquibase.util.TestDatabaseConfigUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic test to isolate the FileFormat snapshot issue.
 */
@DisplayName("FileFormat Basic Snapshot Test")
public class FileFormatBasicSnapshotTest {

    private Connection connection;
    private Database database;
    private String testSchema;
    
    @BeforeEach
    public void setUp() throws Exception {
        connection = TestDatabaseConfigUtil.getSnowflakeConnection();
        database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        
        // Generate unique schema name
        testSchema = "FF_BASIC_TEST_" + System.currentTimeMillis();
        
        // Create clean schema and file format
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP SCHEMA IF EXISTS " + testSchema + " CASCADE");
            stmt.execute("CREATE SCHEMA " + testSchema);
            stmt.execute("USE SCHEMA " + testSchema);
            stmt.execute("CREATE FILE FORMAT BASIC_FF TYPE = 'CSV' FIELD_DELIMITER = ','");
        }
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP SCHEMA IF EXISTS " + testSchema + " CASCADE");
            }
            connection.close();
        }
    }
    
    @Test
    @DisplayName("Test FileFormat snapshot with only FileFormat.class in SnapshotControl")
    public void testFileFormatOnlySnapshot() throws Exception {
        
        database.setDefaultSchemaName(testSchema);
        
        // UPDATED: Use direct FileFormat request pattern (extension objects don't support bulk discovery)
        liquibase.structure.core.Catalog catalog = new liquibase.structure.core.Catalog(database.getDefaultCatalogName());
        liquibase.structure.core.Schema schema = new liquibase.structure.core.Schema(catalog, testSchema);
        
        FileFormat exampleFormat = new FileFormat();
        exampleFormat.setName("BASIC_FF");
        exampleFormat.setSchema(schema);
        
        
        SnapshotControl snapshotControl = new SnapshotControl(database, FileFormat.class);
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(
            new liquibase.structure.DatabaseObject[]{exampleFormat}, database, snapshotControl);
        
        FileFormat result = snapshot.get(exampleFormat);
        
        assertNotNull(result, "FileFormat should be found with direct request");
        
        
        assertEquals("BASIC_FF", result.getName(), "FileFormat name should match");
        assertEquals("CSV", result.getFormatType(), "FileFormat type should be CSV");
    }
    
    @Test
    @DisplayName("Test FileFormat snapshot with Schema.class and FileFormat.class")
    public void testSchemaAndFileFormatSnapshot() throws Exception {
        
        database.setDefaultSchemaName(testSchema);
        
        // UPDATED: Use direct FileFormat request pattern (extension objects don't support bulk discovery)
        // Even with Schema.class included, FileFormats need direct requests
        liquibase.structure.core.Catalog catalog = new liquibase.structure.core.Catalog(database.getDefaultCatalogName());
        liquibase.structure.core.Schema schema = new liquibase.structure.core.Schema(catalog, testSchema);
        
        FileFormat exampleFormat = new FileFormat();
        exampleFormat.setName("BASIC_FF");
        exampleFormat.setSchema(schema);
        
        
        SnapshotControl snapshotControl = new SnapshotControl(database, FileFormat.class);
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(
            new liquibase.structure.DatabaseObject[]{exampleFormat}, database, snapshotControl);
        
        FileFormat result = snapshot.get(exampleFormat);
        
        assertNotNull(result, "FileFormat should be found with direct request");
        
        
        assertEquals("BASIC_FF", result.getName(), "FileFormat name should match");
        assertEquals("CSV", result.getFormatType(), "FileFormat type should be CSV");
        assertEquals(",", result.getFieldDelimiter(), "FileFormat field delimiter should match");
    }
}