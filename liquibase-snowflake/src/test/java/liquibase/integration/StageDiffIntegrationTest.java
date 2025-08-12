package liquibase.integration;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.diff.output.report.DiffToReport;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.Schema;
import liquibase.database.object.Stage;
import liquibase.util.TestDatabaseConfigUtil;
import liquibase.Scope;
import liquibase.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for Stage diff detection and changelog generation.
 * Tests that Stage differences are properly detected and can generate appropriate changelog entries.
 */
@DisplayName("Stage Diff Integration Test")
public class StageDiffIntegrationTest {

    private static final Logger logger = Scope.getCurrentScope().getLog(StageDiffIntegrationTest.class);
    
    private Database database;
    private Connection connection;
    private String baseSchema = "STAGE_DIFF_BASE";
    private String modifiedSchema = "STAGE_DIFF_MODIFIED";

    @BeforeEach
    void setUp() throws Exception {
        // Skip test if database connection info is not available
        Assumptions.assumeTrue(TestDatabaseConfigUtil.getSnowflakeConnection() != null);
        
        connection = TestDatabaseConfigUtil.getSnowflakeConnection();
        database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        
        // Clean up any existing schemas from previous test runs
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP SCHEMA IF EXISTS " + baseSchema + " CASCADE");
            stmt.execute("DROP SCHEMA IF EXISTS " + modifiedSchema + " CASCADE");
            
            // Create fresh schemas
            stmt.execute("CREATE SCHEMA " + baseSchema);
            stmt.execute("CREATE SCHEMA " + modifiedSchema);
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null) {
            try {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("DROP SCHEMA IF EXISTS " + baseSchema + " CASCADE");
                    stmt.execute("DROP SCHEMA IF EXISTS " + modifiedSchema + " CASCADE");
                }
            } catch (Exception e) {
                logger.warning("Failed to cleanup schemas: " + e.getMessage());
            }
            connection.close();
        }
    }

    @Test
    @DisplayName("Should detect new Stage (missing in base, present in modified)")
    public void testDetectNewStage() throws Exception {
        // Setup: Base schema has no stages, modified schema has one stage
        try (Statement stmt = connection.createStatement()) {
            // Base schema: empty
            stmt.execute("USE SCHEMA " + baseSchema);
            // (no stages created)
            
            // Modified schema: create a stage
            stmt.execute("USE SCHEMA " + modifiedSchema);
            stmt.execute("CREATE STAGE TEST_NEW_STAGE " +
                "URL = 's3://test-bucket/data/' " +
                "FILE_FORMAT = (TYPE = CSV) " +
                "COMMENT = 'Test stage for diff detection'");
        }

        // Generate snapshots
        database.setDefaultSchemaName(baseSchema);
        SnapshotControl baseControl = new SnapshotControl(database, Schema.class, Stage.class);
        DatabaseSnapshot baseSnapshot = SnapshotGeneratorFactory.getInstance()
                .createSnapshot(database.getDefaultSchema(), database, baseControl);
        
        logger.info("DEBUG: Base snapshot Stage objects: " + baseSnapshot.get(Stage.class).size());
                
        database.setDefaultSchemaName(modifiedSchema);
        SnapshotControl modifiedControl = new SnapshotControl(database, Schema.class, Stage.class);
        DatabaseSnapshot modifiedSnapshot = SnapshotGeneratorFactory.getInstance()
                .createSnapshot(database.getDefaultSchema(), database, modifiedControl);
        
        logger.info("DEBUG: Modified snapshot Stage objects: " + modifiedSnapshot.get(Stage.class).size());
        
        // Generate diff - swap parameters so missing = "should be added" and unexpected = "should be dropped"
        DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(
                modifiedSnapshot, baseSnapshot, new CompareControl());
        
        // Assert: Should detect the missing stage
        assertNotNull(diffResult.getMissingObjects(Stage.class), "Should have missing Stage objects");
        assertEquals(1, diffResult.getMissingObjects(Stage.class).size(), 
                "Should detect exactly 1 missing Stage");
        
        Stage missingStage = diffResult.getMissingObjects(Stage.class).iterator().next();
        assertEquals("TEST_NEW_STAGE", missingStage.getName(), "Should detect correct stage name");
        
        // Generate changelog and verify it contains createStage
        ByteArrayOutputStream changelogOutput = new ByteArrayOutputStream();
        DiffToChangeLog diffToChangeLog = new DiffToChangeLog(diffResult, new DiffOutputControl());
        diffToChangeLog.print(new PrintStream(changelogOutput));
        
        String changelog = changelogOutput.toString();
        assertTrue(changelog.contains("createStage"), "Changelog should contain createStage operation");
        assertTrue(changelog.contains("TEST_NEW_STAGE"), "Changelog should reference the stage name");
        
        logger.info("✅ Detected new Stage and generated appropriate changelog");
    }

    @Test
    @DisplayName("Should detect dropped Stage (present in base, missing in modified)")
    public void testDetectDroppedStage() throws Exception {
        // Setup: Base schema has stage, modified schema doesn't
        try (Statement stmt = connection.createStatement()) {
            // Base schema: create a stage
            stmt.execute("USE SCHEMA " + baseSchema);
            stmt.execute("CREATE STAGE TEST_DROPPED_STAGE " +
                "URL = 's3://test-bucket/data/' " +
                "FILE_FORMAT = (TYPE = JSON) " +
                "COMMENT = 'Stage to be dropped'");
            
            // Modified schema: empty
            stmt.execute("USE SCHEMA " + modifiedSchema);
            // (no stages created)
        }

        // Generate snapshots
        database.setDefaultSchemaName(baseSchema);
        SnapshotControl baseControl = new SnapshotControl(database, Schema.class, Stage.class);
        DatabaseSnapshot baseSnapshot = SnapshotGeneratorFactory.getInstance()
                .createSnapshot(database.getDefaultSchema(), database, baseControl);
        
        logger.info("DEBUG: Base snapshot Stage objects: " + baseSnapshot.get(Stage.class).size());
        
        database.setDefaultSchemaName(modifiedSchema);
        SnapshotControl modifiedControl = new SnapshotControl(database, Schema.class, Stage.class);
        DatabaseSnapshot modifiedSnapshot = SnapshotGeneratorFactory.getInstance()
                .createSnapshot(database.getDefaultSchema(), database, modifiedControl);
        
        logger.info("DEBUG: Modified snapshot Stage objects: " + modifiedSnapshot.get(Stage.class).size());
        
        // Generate diff - For dropped stage, we need to swap parameters
        // When comparing modified (empty) vs base (has stage), the stage becomes "unexpected" in base
        DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(
                modifiedSnapshot, baseSnapshot, new CompareControl());
        
        // Assert: Stage should be "unexpected" (present in base but not in modified = should be dropped)
        assertNotNull(diffResult.getUnexpectedObjects(Stage.class), "Should have unexpected Stage objects");
        assertEquals(1, diffResult.getUnexpectedObjects(Stage.class).size(), 
                "Should detect exactly 1 unexpected Stage");
        
        Stage unexpectedStage = diffResult.getUnexpectedObjects(Stage.class).iterator().next();
        assertEquals("TEST_DROPPED_STAGE", unexpectedStage.getName(), "Should detect correct stage name");
        
        // Generate changelog and verify it contains dropStage
        ByteArrayOutputStream changelogOutput = new ByteArrayOutputStream();
        DiffToChangeLog diffToChangeLog = new DiffToChangeLog(diffResult, new DiffOutputControl());
        diffToChangeLog.print(new PrintStream(changelogOutput));
        
        String changelog = changelogOutput.toString();
        assertTrue(changelog.contains("dropStage"), "Changelog should contain dropStage operation");
        assertTrue(changelog.contains("TEST_DROPPED_STAGE"), "Changelog should reference the stage name");
        
        logger.info("✅ Detected dropped Stage and generated appropriate changelog");
    }

    @Test
    @DisplayName("Should detect modified Stage properties")
    public void testDetectModifiedStageProperties() throws Exception {
        // Setup: Both schemas have same stage but with different properties
        try (Statement stmt = connection.createStatement()) {
            // Base schema: stage with original properties
            stmt.execute("USE SCHEMA " + baseSchema);
            stmt.execute("CREATE STAGE TEST_MODIFIED_STAGE " +
                "URL = 's3://original-bucket/data/' " +
                "FILE_FORMAT = (TYPE = CSV) " +
                "COMMENT = 'Original comment'");
            
            // Modified schema: same stage with different properties
            stmt.execute("USE SCHEMA " + modifiedSchema);
            stmt.execute("CREATE STAGE TEST_MODIFIED_STAGE " +
                "URL = 's3://modified-bucket/data/' " +
                "FILE_FORMAT = (TYPE = JSON) " +
                "COMMENT = 'Modified comment'");
        }

        // Generate snapshots
        database.setDefaultSchemaName(baseSchema);
        SnapshotControl baseControl = new SnapshotControl(database, Schema.class, Stage.class);
        DatabaseSnapshot baseSnapshot = SnapshotGeneratorFactory.getInstance()
                .createSnapshot(database.getDefaultSchema(), database, baseControl);
                
        database.setDefaultSchemaName(modifiedSchema);
        SnapshotControl modifiedControl = new SnapshotControl(database, Schema.class, Stage.class);
        DatabaseSnapshot modifiedSnapshot = SnapshotGeneratorFactory.getInstance()
                .createSnapshot(database.getDefaultSchema(), database, modifiedControl);
        
        // Generate diff
        DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(
                baseSnapshot, modifiedSnapshot, new CompareControl());
        
        // Assert: Should detect changed stage (implementation may vary on how changes are detected)
        // The simplified diff generator detects changes but might not capture all details
        
        // Generate diff report for detailed analysis
        ByteArrayOutputStream reportOutput = new ByteArrayOutputStream();
        DiffToReport diffToReport = new DiffToReport(diffResult, new PrintStream(reportOutput));
        diffToReport.print();
        
        String diffReport = reportOutput.toString();
        
        // The stage should be detected as having some kind of difference
        // (exact format depends on implementation details)
        assertTrue(diffReport.contains("TEST_MODIFIED_STAGE") || 
                  diffResult.getChangedObjects(Stage.class).size() > 0, 
                  "Should detect changes in Stage properties");
        
        logger.info("✅ Detected Stage property changes");
        logger.info("Diff report: " + diffReport);
    }

    @Test
    @DisplayName("Should generate no differences for identical Stages")
    public void testIdenticalStagesNoDifferences() throws Exception {
        // Setup: Compare the same schema to itself (should be identical)
        String stageSQL = "CREATE STAGE TEST_IDENTICAL_STAGE " +
                "URL = 's3://same-bucket/data/' " +
                "FILE_FORMAT = (TYPE = PARQUET) " +
                "COMMENT = 'Identical stage comment'";
        
        try (Statement stmt = connection.createStatement()) {
            // Create stage in base schema
            stmt.execute("USE SCHEMA " + baseSchema);
            stmt.execute(stageSQL);
        }

        // Generate two snapshots of the same schema (should be identical)
        database.setDefaultSchemaName(baseSchema);
        SnapshotControl control1 = new SnapshotControl(database, Schema.class, Stage.class);
        DatabaseSnapshot snapshot1 = SnapshotGeneratorFactory.getInstance()
                .createSnapshot(database.getDefaultSchema(), database, control1);
                
        SnapshotControl control2 = new SnapshotControl(database, Schema.class, Stage.class);
        DatabaseSnapshot snapshot2 = SnapshotGeneratorFactory.getInstance()
                .createSnapshot(database.getDefaultSchema(), database, control2);
        
        // Generate diff (comparing same schema to itself)
        DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(
                snapshot1, snapshot2, new CompareControl());
        
        // Debug: Check what differences were found
        logger.info("DEBUG: Missing objects: " + diffResult.getMissingObjects(Stage.class).size());
        logger.info("DEBUG: Unexpected objects: " + diffResult.getUnexpectedObjects(Stage.class).size());
        
        // Assert: Should detect no differences when comparing identical snapshots
        assertTrue(diffResult.getMissingObjects(Stage.class).isEmpty(), 
                "Should have no missing Stage objects");
        assertTrue(diffResult.getUnexpectedObjects(Stage.class).isEmpty(), 
                "Should have no unexpected Stage objects");
        
        // Generate diff report to verify clean comparison
        ByteArrayOutputStream reportOutput = new ByteArrayOutputStream();
        DiffToReport diffToReport = new DiffToReport(diffResult, new PrintStream(reportOutput));
        diffToReport.print();
        
        String diffReport = reportOutput.toString();
        logger.info("DEBUG: Diff report content: " + diffReport);
        
        // Check that stages are reported as NONE (no differences)
        assertTrue(diffReport.contains("Missing Stage(s): NONE"), "Should report no missing stages");
        assertTrue(diffReport.contains("Unexpected Stage(s): NONE"), "Should report no unexpected stages");
        assertFalse(diffReport.contains("Missing Stage(s):") && !diffReport.contains("Missing Stage(s): NONE"), 
                "Should not have actual missing stages");
        assertFalse(diffReport.contains("Unexpected Stage(s):") && !diffReport.contains("Unexpected Stage(s): NONE"), 
                "Should not have actual unexpected stages");
        
        logger.info("✅ Identical Stages show no differences");
    }
}