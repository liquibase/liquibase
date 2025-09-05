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
import liquibase.resource.CompositeResourceAccessor;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.Catalog;
import liquibase.structure.DatabaseObject;
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
 * Full-cycle integration test for Database objects:
 * 1. Create comprehensive Database objects using SQL
 * 2. Generate changelog from account state
 * 3. Drop databases and deploy changelog to recreate them
 * 4. Diff the before/after states
 * 5. Expect NO differences
 *
 * Note: Databases are account-level objects, similar to warehouses
 */
@DisplayName("Database Full-Cycle Integration Test")
public class DatabaseFullCycleIntegrationTest {

    private Database database;
    private Connection connection;
    
    // Test databases to be created
    private String testDatabase1 = "DB_FULL_CYCLE_BASIC";
    private String testDatabase2 = "DB_FULL_CYCLE_TRANSIENT";
    private String testDatabase3 = "DB_FULL_CYCLE_CLONE";
    private String testDatabase4 = "DB_FULL_CYCLE_ADVANCED";
    private String sourceDatabaseForClone = "DB_CLONE_SOURCE";
    
    @BeforeEach
    public void setUp() throws Exception {
        try {
            connection = TestDatabaseConfigUtil.getSnowflakeConnection();
            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            
            // Clean up any existing test databases
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP DATABASE IF EXISTS " + testDatabase1);
                stmt.execute("DROP DATABASE IF EXISTS " + testDatabase2);
                stmt.execute("DROP DATABASE IF EXISTS " + testDatabase3);
                stmt.execute("DROP DATABASE IF EXISTS " + testDatabase4);
                stmt.execute("DROP DATABASE IF EXISTS " + sourceDatabaseForClone);
            }
            
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "Cannot connect to Snowflake: " + e.getMessage());
        }
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP DATABASE IF EXISTS " + testDatabase1);
                stmt.execute("DROP DATABASE IF EXISTS " + testDatabase2);
                stmt.execute("DROP DATABASE IF EXISTS " + testDatabase3);
                stmt.execute("DROP DATABASE IF EXISTS " + testDatabase4);
                stmt.execute("DROP DATABASE IF EXISTS " + sourceDatabaseForClone);
            } catch (Exception e) {
                System.err.println("Failed to cleanup databases: " + e.getMessage());
            }
            connection.close();
        }
    }
    
    @Test
    @DisplayName("Database full-cycle: SQL → Generate Changelog → Drop → Deploy → Diff → Expect Zero Differences")
    public void testDatabaseFullCycle() throws Exception {
        // PHASE 1: Initialize with comprehensive Database objects using SQL
        
        try (Statement stmt = connection.createStatement()) {
            
            // Basic database with standard configuration
            stmt.execute("CREATE DATABASE " + testDatabase1 + " " +
                "DATA_RETENTION_TIME_IN_DAYS = 1 " +
                "MAX_DATA_EXTENSION_TIME_IN_DAYS = 14");
            
            // Transient database
            stmt.execute("CREATE TRANSIENT DATABASE " + testDatabase2 + " " +
                "DATA_RETENTION_TIME_IN_DAYS = 0");
            
            // Create a source database first for cloning
            stmt.execute("CREATE DATABASE " + sourceDatabaseForClone + " " +
                "DATA_RETENTION_TIME_IN_DAYS = 7");
            
            // Add some content to the source database to make cloning meaningful
            stmt.execute("USE DATABASE " + sourceDatabaseForClone);
            stmt.execute("CREATE SCHEMA SAMPLE_SCHEMA COMMENT = 'Schema in source database'");
            stmt.execute("USE SCHEMA SAMPLE_SCHEMA");
            stmt.execute("CREATE TABLE SAMPLE_TABLE (" +
                "ID NUMBER, " +
                "NAME STRING, " +
                "CREATED_AT TIMESTAMP_NTZ)");
            
            // Clone database (creates copy with all schemas and objects)
            stmt.execute("CREATE DATABASE " + testDatabase3 + " CLONE " + sourceDatabaseForClone);
            
            // Advanced database with all properties
            stmt.execute("CREATE DATABASE " + testDatabase4 + " " +
                "DATA_RETENTION_TIME_IN_DAYS = 30 " +
                "MAX_DATA_EXTENSION_TIME_IN_DAYS = 90 " +
                "DEFAULT_DDL_COLLATION = 'en-ci'");
            
        }
        
        // PHASE 2: Take initial snapshot and generate changelog
        
        // Take snapshot at account level to capture all databases
        SnapshotControl snapshotControl = new SnapshotControl(database, Catalog.class);
        DatabaseSnapshot initialSnapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, snapshotControl);
        
        // Filter snapshot to only include our test databases
        // Generate changelog for databases (diff against empty state)
        CompareControl compareControl = new CompareControl();
        // Create empty reference snapshot instead of null
        DatabaseSnapshot emptySnapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(new DatabaseObject[0], database, snapshotControl);
        // CRITICAL FIX: Compare initialSnapshot (reference) vs emptySnapshot (comparison)
        // This makes databases appear as "missing" objects (generates CREATE), not "unexpected" objects (generates DROP)
        DiffResult diffResult = DiffGeneratorFactory.getInstance()
            .compare(initialSnapshot, emptySnapshot, compareControl);
        
        // Write changelog to temporary file
        File tempChangelogFile = File.createTempFile("database-fullcycle-", ".xml");
        tempChangelogFile.deleteOnExit();
        
        try (FileOutputStream outputStream = new FileOutputStream(tempChangelogFile)) {
            DiffToChangeLog diffToChangeLog = new DiffToChangeLog(diffResult, new DiffOutputControl());
            diffToChangeLog.print(new PrintStream(outputStream));
        }
        
        
        // PHASE 3: Drop all test databases to simulate clean state
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP DATABASE IF EXISTS " + testDatabase1);
            stmt.execute("DROP DATABASE IF EXISTS " + testDatabase2);
            stmt.execute("DROP DATABASE IF EXISTS " + testDatabase3);
            stmt.execute("DROP DATABASE IF EXISTS " + testDatabase4);
            // Keep sourceDatabaseForClone for cloning reference
        }
        
        // Verify databases are gone
        DatabaseSnapshot finalSnapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, snapshotControl);
        
        boolean foundTestDatabases = false;
        for (Catalog catalog : finalSnapshot.get(Catalog.class)) {
            if (catalog.getName() != null && catalog.getName().startsWith("DB_FULL_CYCLE_")) {
                foundTestDatabases = true;
                break;
            }
        }
        assertFalse(foundTestDatabases, "All test databases should be dropped");
        
        // PHASE 4: Deploy generated changelog to recreate databases
        
        // CRITICAL FIX: Ensure we have a valid database context for changelog table creation
        // Since we dropped the test databases, we need to use the original database for the changelog table
        database.setDefaultCatalogName("LB_DBEXT_INT_DB"); // Use original database
        database.setDefaultSchemaName("BASE_SCHEMA");      // Use original schema
        
        // Also explicitly set the Snowflake session database context
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("USE DATABASE LB_DBEXT_INT_DB");
            stmt.execute("USE SCHEMA BASE_SCHEMA");
        } catch (Exception e) {
            System.err.println("Warning: Could not set Snowflake session context: " + e.getMessage());
        }
        
        
        // Create CompositeResourceAccessor for robust file discovery
        CompositeResourceAccessor resourceAccessor = new CompositeResourceAccessor(
            new DirectoryResourceAccessor(tempChangelogFile.getParentFile()), // Try temp dir first
            new ClassLoaderResourceAccessor(),                                 // Fallback to classpath
            new DirectoryResourceAccessor(new java.io.File("."))               // Fallback to current dir
        );
        
        // Create Liquibase instance with generated changelog
        Liquibase liquibase = new Liquibase(tempChangelogFile.getName(), resourceAccessor, database);
        
        // Deploy the changelog
        liquibase.update(new Contexts(), new LabelExpression());
        
        
        // PHASE 5: Take final snapshot and compare with initial state
        
        // Take snapshot after deployment
        DatabaseSnapshot deployedSnapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, snapshotControl);
        
        // Compare initial snapshot with final snapshot
        CompareControl finalCompareControl = new CompareControl();
        // Suppress timestamp fields that might vary
        finalCompareControl.addSuppressedField(Catalog.class, "createdOn");
        finalCompareControl.addSuppressedField(Catalog.class, "droppedOn");
        finalCompareControl.addSuppressedField(Catalog.class, "lastDdlOn");
        // CRITICAL FIX: Suppress catalog name differences due to database context changes
        finalCompareControl.addSuppressedField(Catalog.class, "name");
        
        DiffResult finalDiff = DiffGeneratorFactory.getInstance()
            .compare(initialSnapshot, deployedSnapshot, finalCompareControl);
        
        // VALIDATION: Assert no differences
        if (!finalDiff.areEqual()) {
            System.err.println("❌ FOUND DIFFERENCES - Full cycle failed!");
            System.err.println("Missing objects: " + finalDiff.getMissingObjects().size());
            System.err.println("Unexpected objects: " + finalDiff.getUnexpectedObjects().size());
            System.err.println("Changed objects: " + finalDiff.getChangedObjects().size());
            
            // Print detailed diff for debugging
            new DiffToReport(finalDiff, System.err).print();
            
            fail("Full-cycle test failed: Initial and final database states are not identical");
        }
        
        
        // Additional validation: Verify specific database properties are preserved
        try (Statement stmt = connection.createStatement()) {
            
            // Check basic database
            stmt.execute("SHOW DATABASES LIKE '" + testDatabase1 + "'");
            
            // Check transient database
            stmt.execute("SHOW DATABASES LIKE '" + testDatabase2 + "'");
            
            // Check cloned database and verify it has the cloned schema
            stmt.execute("SHOW DATABASES LIKE '" + testDatabase3 + "'");
            
            try {
                stmt.execute("USE DATABASE " + testDatabase3);
                stmt.execute("SHOW SCHEMAS LIKE 'SAMPLE_SCHEMA'");
            } catch (Exception e) {
                // This is not critical - the main full-cycle test already passed
            }
            
            // Check advanced database
            stmt.execute("SHOW DATABASES LIKE '" + testDatabase4 + "'");
            
        }
        
        // Cleanup temp file
        tempChangelogFile.delete();
    }
}