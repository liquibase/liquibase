package liquibase.integration;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.diff.output.report.DiffToReport;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.DirectoryResourceAccessor;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.Schema;
import liquibase.database.object.FileFormat;
import liquibase.util.TestDatabaseConfigUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Full-cycle integration test for FileFormat objects:
 * 1. Initialize schema with SQL statements (all FileFormat variations)
 * 2. Generate changelog from schema
 * 3. Deploy changelog to clean second schema
 * 4. Diff the two schemas
 * 5. Expect NO differences
 *
 * This validates the complete round-trip: SQL → Snapshot → Diff → ChangeLog → Deploy → Validate
 */
@DisplayName("FileFormat Full-Cycle Integration Test")
public class FileFormatFullCycleIntegrationTest {

    private Database database;
    private Connection connection;
    private String sourceSchema = "FF_FULL_CYCLE_SOURCE";
    private String targetSchema = "FF_FULL_CYCLE_TARGET";
    
    @BeforeEach
    public void setUp() throws Exception {
        try {
            connection = TestDatabaseConfigUtil.getSnowflakeConnection();
            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            
            // Create clean schemas for testing
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP SCHEMA IF EXISTS " + sourceSchema + " CASCADE");
                stmt.execute("DROP SCHEMA IF EXISTS " + targetSchema + " CASCADE");
                stmt.execute("CREATE SCHEMA " + sourceSchema);
                stmt.execute("CREATE SCHEMA " + targetSchema);
            }
            
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "Cannot connect to Snowflake or create schemas: " + e.getMessage());
        }
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP SCHEMA IF EXISTS " + sourceSchema + " CASCADE");
                stmt.execute("DROP SCHEMA IF EXISTS " + targetSchema + " CASCADE");
            } catch (Exception e) {
                System.err.println("Failed to cleanup schemas: " + e.getMessage());
            }
            connection.close();
        }
    }
    
    @Test
    @DisplayName("FileFormat full-cycle: SQL → Generate Changelog → Deploy → Diff → Expect Zero Differences")
    public void testFileFormatFullCycle() throws Exception {
        // PHASE 1: Initialize source schema with comprehensive FileFormat objects using SQL
        
        try (Statement stmt = connection.createStatement()) {
            // Use the source schema
            stmt.execute("USE SCHEMA " + sourceSchema);
            
            // CSV FileFormat with all major properties
            stmt.execute("CREATE FILE FORMAT FF_CSV_COMPREHENSIVE " +
                "TYPE = 'CSV' " +
                "COMPRESSION = 'GZIP' " +
                "RECORD_DELIMITER = '\\n' " +
                "FIELD_DELIMITER = ',' " +
                "FILE_EXTENSION = '.csv.gz' " +
                "SKIP_HEADER = 1 " +
                "SKIP_BLANK_LINES = TRUE " +
                "DATE_FORMAT = 'YYYY-MM-DD' " +
                "TIME_FORMAT = 'HH24:MI:SS' " +
                "TIMESTAMP_FORMAT = 'YYYY-MM-DD HH24:MI:SS' " +
                "BINARY_FORMAT = 'HEX' " +
                "ESCAPE = '\\\\' " +
                "ESCAPE_UNENCLOSED_FIELD = '\\\\' " +
                "TRIM_SPACE = TRUE " +
                "FIELD_OPTIONALLY_ENCLOSED_BY = '\"' " +
                "NULL_IF = ('NULL', 'null', '') " +
                "ERROR_ON_COLUMN_COUNT_MISMATCH = FALSE " +
                "REPLACE_INVALID_CHARACTERS = TRUE " +
                "EMPTY_FIELD_AS_NULL = TRUE " +
                "SKIP_BYTE_ORDER_MARK = TRUE " +
                "ENCODING = 'UTF-8'");
            
            // JSON FileFormat with specific properties
            stmt.execute("CREATE FILE FORMAT FF_JSON_COMPLETE " +
                "TYPE = 'JSON' " +
                "COMPRESSION = 'AUTO' " +
                "DATE_FORMAT = 'AUTO' " +
                "TIME_FORMAT = 'AUTO' " +
                "TIMESTAMP_FORMAT = 'AUTO' " +
                "BINARY_FORMAT = 'BASE64' " +
                "TRIM_SPACE = FALSE " +
                "NULL_IF = ('null') " +
                "FILE_EXTENSION = '.json' " +
                "ENABLE_OCTAL = FALSE " +
                "ALLOW_DUPLICATE = FALSE " +
                "STRIP_OUTER_ARRAY = TRUE " +
                "STRIP_NULL_VALUES = TRUE " +
                "IGNORE_UTF8_ERRORS = TRUE " +
                "SKIP_BYTE_ORDER_MARK = FALSE");
            
            // PARQUET FileFormat (minimal properties)
            stmt.execute("CREATE FILE FORMAT FF_PARQUET_BASIC " +
                "TYPE = 'PARQUET' " +
                "COMPRESSION = 'SNAPPY' " +
                "BINARY_AS_TEXT = FALSE " +
                "TRIM_SPACE = TRUE " +
                "NULL_IF = ('NULL')");
            
            // XML FileFormat with complex settings
            stmt.execute("CREATE FILE FORMAT FF_XML_ADVANCED " +
                "TYPE = 'XML' " +
                "COMPRESSION = 'BROTLI' " +
                "IGNORE_UTF8_ERRORS = FALSE " +
                "PRESERVE_SPACE = TRUE " +
                "STRIP_OUTER_ELEMENT = FALSE " +
                "DISABLE_SNOWFLAKE_DATA = FALSE " +
                "DISABLE_AUTO_CONVERT = TRUE " +
                "SKIP_BYTE_ORDER_MARK = TRUE");
            
        }
        
        // PHASE 2: Generate changelog from source schema
        
        // Set database to use source schema
        database.setDefaultSchemaName(sourceSchema);
        
        // Take snapshot of source schema - include Schema objects to enable FileFormat discovery
        SnapshotControl snapshotControl = new SnapshotControl(database, Schema.class, FileFormat.class);
        DatabaseSnapshot sourceSnapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, snapshotControl);
        
        // Create empty snapshot for comparison
        database.setDefaultSchemaName(targetSchema); // Switch to empty target schema  
        SnapshotControl emptySnapshotControl = new SnapshotControl(database, Schema.class, FileFormat.class);
        DatabaseSnapshot emptySnapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, emptySnapshotControl);
        database.setDefaultSchemaName(sourceSchema); // Switch back to source
        
        // Generate diff between empty schema and source schema
        CompareControl compareControl = new CompareControl();
        DiffResult diffResult = DiffGeneratorFactory.getInstance()
            // CRITICAL FIX: Compare sourceSnapshot (reference) vs emptySnapshot (comparison)
            // This makes file formats appear as "missing" objects (generates CREATE), not "unexpected" objects (generates DROP)
            .compare(sourceSnapshot, emptySnapshot, compareControl);
        
        // Write diff to temporary changelog file
        File tempChangelogFile = File.createTempFile("fileformat-fullcycle-", ".xml");
        tempChangelogFile.deleteOnExit();
        
        try (FileOutputStream outputStream = new FileOutputStream(tempChangelogFile)) {
            DiffToChangeLog diffToChangeLog = new DiffToChangeLog(diffResult, new DiffOutputControl());
            diffToChangeLog.print(new PrintStream(outputStream));
        }
        
        
        // PHASE 3: Deploy generated changelog to target schema
        
        // Switch database to target schema
        database.setDefaultSchemaName(targetSchema);
        
        // Create Liquibase instance with generated changelog
        DirectoryResourceAccessor resourceAccessor = new DirectoryResourceAccessor(tempChangelogFile.getParentFile());
        Liquibase liquibase = new Liquibase(tempChangelogFile.getName(), resourceAccessor, database);
        
        // Deploy the generated changelog
        liquibase.update(new Contexts(), new LabelExpression());
        
        
        // PHASE 4: Take snapshots of both schemas and compare
        
        // Take snapshot of target schema (after deployment)
        SnapshotControl finalSnapshotControl = new SnapshotControl(database, Schema.class, FileFormat.class);
        DatabaseSnapshot targetSnapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, finalSnapshotControl);
        
        // Reset database to source schema and take fresh snapshot
        database.setDefaultSchemaName(sourceSchema);
        DatabaseSnapshot freshSourceSnapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, finalSnapshotControl);
        
        // PHASE 5: Compare snapshots - should be identical
        
        // Compare the snapshots
        CompareControl finalCompareControl = new CompareControl();
        // Suppress schema names in comparison since they're different
        finalCompareControl.addSuppressedField(Schema.class, "name");
        
        DiffResult finalDiff = DiffGeneratorFactory.getInstance()
            .compare(freshSourceSnapshot, targetSnapshot, finalCompareControl);
        
        // VALIDATION: Assert no differences
        if (!finalDiff.areEqual()) {
            System.err.println("❌ FOUND DIFFERENCES - Full cycle failed!");
            System.err.println("Missing objects: " + finalDiff.getMissingObjects().size());
            System.err.println("Unexpected objects: " + finalDiff.getUnexpectedObjects().size());
            System.err.println("Changed objects: " + finalDiff.getChangedObjects().size());
            
            // Print detailed diff for debugging
            new DiffToReport(finalDiff, System.err).print();
            
            fail("Full-cycle test failed: Source and target schemas are not identical after round-trip");
        }
        
        
        // Cleanup temp file
        tempChangelogFile.delete();
    }
}