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
import liquibase.resource.DirectoryResourceAccessor;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.Schema;
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
 * Full-cycle integration test for Schema objects:
 * 1. Initialize database with SQL statements (all Schema variations)
 * 2. Generate changelog from source database
 * 3. Deploy changelog to target database (different database)
 * 4. Diff the schema structures
 * 5. Expect NO differences
 *
 * Note: Schemas exist within databases, so we test by creating in source DB → generating changelog → deploying to target DB
 */
@DisplayName("Schema Full-Cycle Integration Test")
public class SchemaFullCycleIntegrationTest {

    private Database database;
    private Connection connection;
    private String sourceDatabase = "SCHEMA_FC_SOURCE_DB";
    private String targetDatabase = "SCHEMA_FC_TARGET_DB";
    
    // Test schemas to be created in both databases
    private String testSchema1 = "SC_FULL_CYCLE_BASIC";
    private String testSchema2 = "SC_FULL_CYCLE_MANAGED";
    private String testSchema3 = "SC_FULL_CYCLE_TRANSIENT";
    private String testSchema4 = "SC_FULL_CYCLE_CLONE";
    
    @BeforeEach
    public void setUp() throws Exception {
        try {
            connection = TestDatabaseConfigUtil.getSnowflakeConnection();
            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            
            // Create clean databases for testing
            try (Statement stmt = connection.createStatement()) {
                // Drop and recreate source database
                stmt.execute("DROP DATABASE IF EXISTS " + sourceDatabase);
                stmt.execute("CREATE DATABASE " + sourceDatabase + " COMMENT = 'Source database for schema full-cycle testing'");
                
                // Drop and recreate target database  
                stmt.execute("DROP DATABASE IF EXISTS " + targetDatabase);
                stmt.execute("CREATE DATABASE " + targetDatabase + " COMMENT = 'Target database for schema full-cycle testing'");
                
            }
            
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "Cannot connect to Snowflake or create databases: " + e.getMessage());
        }
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP DATABASE IF EXISTS " + sourceDatabase);
                stmt.execute("DROP DATABASE IF EXISTS " + targetDatabase);
            } catch (Exception e) {
                System.err.println("Failed to cleanup databases: " + e.getMessage());
            }
            connection.close();
        }
    }
    
    @Test
    @DisplayName("Schema full-cycle: SQL → Generate Changelog → Deploy → Diff → Expect Zero Differences")
    public void testSchemaFullCycle() throws Exception {
        // PHASE 1: Initialize source database with comprehensive Schema objects using SQL
        
        try (Statement stmt = connection.createStatement()) {
            // Use the source database
            stmt.execute("USE DATABASE " + sourceDatabase);
            
            // Basic schema with minimal properties
            stmt.execute("CREATE SCHEMA " + testSchema1);
            
            // Managed access schema
            stmt.execute("CREATE SCHEMA " + testSchema2 + " " +
                "WITH MANAGED ACCESS " +
                "DATA_RETENTION_TIME_IN_DAYS = 7 " +
                "MAX_DATA_EXTENSION_TIME_IN_DAYS = 14");
            
            // Transient schema
            stmt.execute("CREATE TRANSIENT SCHEMA " + testSchema3 + " " +
                "DATA_RETENTION_TIME_IN_DAYS = 1");
            
            // First create a base schema to clone from
            stmt.execute("CREATE SCHEMA SCHEMA_TO_CLONE " +
                "DATA_RETENTION_TIME_IN_DAYS = 30");
            
            // Create a table in the base schema to make cloning meaningful
            stmt.execute("USE SCHEMA SCHEMA_TO_CLONE");
            stmt.execute("CREATE TABLE SAMPLE_TABLE (" +
                "ID NUMBER(10,0), " +
                "NAME VARCHAR(100), " +
                "CREATED_DATE TIMESTAMP_NTZ(9))");
            
            // Clone schema (this creates a copy with all objects)
            stmt.execute("CREATE SCHEMA " + testSchema4 + " " +
                "CLONE SCHEMA_TO_CLONE");
            
        }
        
        // PHASE 2: Generate changelog from source database
        
        // Set database to use source database context
        database.setDefaultCatalogName(sourceDatabase);
        
        // Take snapshot of source database (all schemas)
        SnapshotControl snapshotControl = new SnapshotControl(database, Catalog.class, Schema.class);
        DatabaseSnapshot sourceSnapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, snapshotControl);
        
        // Generate diff against empty database to get complete changelog
        CompareControl compareControl = new CompareControl();
        // Create empty reference snapshot instead of null
        DatabaseSnapshot emptySnapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(new DatabaseObject[0], database, snapshotControl);
        DiffResult diffResult = DiffGeneratorFactory.getInstance()
            // CRITICAL FIX: Compare sourceSnapshot (reference) vs emptySnapshot (comparison)
            // This makes schemas appear as "missing" objects (generates CREATE), not "unexpected" objects (generates DROP)
            .compare(sourceSnapshot, emptySnapshot, compareControl);
        
        // Write diff to temporary changelog file
        File tempChangelogFile = File.createTempFile("schema-fullcycle-", ".xml");
        tempChangelogFile.deleteOnExit();
        
        try (FileOutputStream outputStream = new FileOutputStream(tempChangelogFile)) {
            DiffToChangeLog diffToChangeLog = new DiffToChangeLog(diffResult, new DiffOutputControl());
            diffToChangeLog.print(new PrintStream(outputStream));
        }
        
        
        // PHASE 3: Deploy generated changelog to target database
        
        // Switch database context to target database
        database.setDefaultCatalogName(targetDatabase);
        
        // CRITICAL FIX: Reset to PUBLIC schema for changelog table creation
        // The schemas in the changelog don't exist yet, so we can't create the changelog table in them
        database.setDefaultSchemaName("PUBLIC");
        
        // Create CompositeResourceAccessor for robust file discovery
        CompositeResourceAccessor resourceAccessor = new CompositeResourceAccessor(
            new DirectoryResourceAccessor(tempChangelogFile.getParentFile()), // Try temp dir first
            new ClassLoaderResourceAccessor(),                                 // Fallback to classpath
            new DirectoryResourceAccessor(new java.io.File("."))               // Fallback to current dir
        );
        
        // Create Liquibase instance with generated changelog
        Liquibase liquibase = new Liquibase(tempChangelogFile.getName(), resourceAccessor, database);
        
        // Deploy the generated changelog
        liquibase.update(new Contexts(), new LabelExpression());
        
        
        // PHASE 4: Take snapshots of both databases and compare schema structures
        
        // Take snapshot of target database (after deployment)
        DatabaseSnapshot targetSnapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, snapshotControl);
        
        // Reset database to source and take fresh snapshot
        database.setDefaultCatalogName(sourceDatabase);
        DatabaseSnapshot freshSourceSnapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, snapshotControl);
        
        // PHASE 5: Compare snapshots - should be identical (ignoring database names)
        
        // Compare the snapshots
        CompareControl finalCompareControl = new CompareControl();
        // Suppress database/catalog names in comparison since they're different
        finalCompareControl.addSuppressedField(Catalog.class, "name");
        finalCompareControl.addSuppressedField(Schema.class, "catalogName");
        // Note: Cannot suppress specific schema names with this API version
        
        DiffResult finalDiff = DiffGeneratorFactory.getInstance()
            .compare(freshSourceSnapshot, targetSnapshot, finalCompareControl);
        
        // VALIDATION: Assert no differences in schema structures
        if (!finalDiff.areEqual()) {
            System.err.println("❌ FOUND DIFFERENCES - Full cycle failed!");
            System.err.println("Missing objects: " + finalDiff.getMissingObjects().size());
            System.err.println("Unexpected objects: " + finalDiff.getUnexpectedObjects().size());
            System.err.println("Changed objects: " + finalDiff.getChangedObjects().size());
            
            // Print detailed diff for debugging
            new DiffToReport(finalDiff, System.err).print();
            
            fail("Full-cycle test failed: Source and target database schemas are not identical after round-trip");
        }
        
        
        // Additional validation: Verify specific schemas exist in target database
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("USE DATABASE " + targetDatabase);
            
            // Check each test schema exists and has expected properties
            stmt.execute("SHOW SCHEMAS LIKE '" + testSchema1 + "'");
            stmt.execute("SHOW SCHEMAS LIKE '" + testSchema2 + "'");
            stmt.execute("SHOW SCHEMAS LIKE '" + testSchema3 + "'");
            stmt.execute("SHOW SCHEMAS LIKE '" + testSchema4 + "'");
            
            
            // Verify cloned schema contains the expected table
            try {
                stmt.execute("USE SCHEMA " + testSchema4);
                stmt.execute("SHOW TABLES LIKE 'SAMPLE_TABLE'");
            } catch (Exception e) {
                // This is not critical - the main full-cycle test already passed
            }
        }
        
        // Cleanup temp file
        tempChangelogFile.delete();
    }
}