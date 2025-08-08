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
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Set;

/**
 * Test direct FileFormat discovery using extension object pattern from documentation
 */
public class FileFormatDirectSnapshotTest {

    private Database database;
    private Connection connection;
    private String testSchema;

    @BeforeEach
    public void setUp() throws Exception {
        try {
            connection = TestDatabaseConfigUtil.getSnowflakeConnection();
            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            
            testSchema = "FF_DIRECT_" + System.currentTimeMillis();
            
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("CREATE SCHEMA " + testSchema);
                stmt.execute("USE SCHEMA " + testSchema);
                stmt.execute("CREATE FILE FORMAT TEST_FF TYPE = 'CSV' FIELD_DELIMITER = ',' COMPRESSION = 'GZIP'");
            }
            
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "Cannot connect to Snowflake: " + e.getMessage());
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP SCHEMA IF EXISTS " + testSchema + " CASCADE");
            } catch (Exception e) {
                System.err.println("Failed to cleanup schema: " + e.getMessage());
            }
            connection.close();
        }
    }

    @Test
    public void testDirectFileFormatSnapshot() throws Exception {
        
        database.setDefaultSchemaName(testSchema);
        
        // Create a FileFormat object as an example for the framework to find
        FileFormat exampleFileFormat = new FileFormat();
        exampleFileFormat.setName("TEST_FF");
        exampleFileFormat.setSchema(new Schema(database.getDefaultCatalogName(), testSchema));
        
        
        // Create snapshot control for direct FileFormat discovery
        SnapshotControl snapshotControl = new SnapshotControl(database, FileFormat.class);
        
        
        // Request specific FileFormat object
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(new liquibase.structure.DatabaseObject[]{exampleFileFormat}, database, snapshotControl);
        
        
        // Check results
        Set<FileFormat> fileFormats = snapshot.get(FileFormat.class);
        
        if (fileFormats != null && !fileFormats.isEmpty()) {
            for (FileFormat ff : fileFormats) {
            }
        } else {
        }
    }
}