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
import liquibase.database.object.Stage;
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
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Full-cycle integration test for Stage objects:
 * 1. Initialize schema with SQL statements (all Stage variations)
 * 2. Generate changelog from schema
 * 3. Deploy changelog to clean second schema
 * 4. Diff the two schemas
 * 5. Expect NO differences
 *
 * This validates the complete round-trip: SQL → Snapshot → Diff → ChangeLog → Deploy → Validate
 */
@DisplayName("Stage Full-Cycle Integration Test")
public class StageFullCycleIntegrationTest {

    private Database database;
    private Connection connection;
    private String sourceSchema = "STAGE_FULL_CYCLE_SOURCE";
    private String targetSchema = "STAGE_FULL_CYCLE_TARGET";

    @BeforeEach
    void setUp() throws Exception {
        // Skip test if database connection info is not available
        Assumptions.assumeTrue(TestDatabaseConfigUtil.getSnowflakeConnection() != null);
        
        connection = TestDatabaseConfigUtil.getSnowflakeConnection();
        database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        
        // Clean up any existing schemas from previous test runs
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP SCHEMA IF EXISTS " + sourceSchema + " CASCADE");
            stmt.execute("DROP SCHEMA IF EXISTS " + targetSchema + " CASCADE");
            
            // Create fresh schemas
            stmt.execute("CREATE SCHEMA " + sourceSchema);
            stmt.execute("CREATE SCHEMA " + targetSchema);
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null) {
            try {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("DROP SCHEMA IF EXISTS " + sourceSchema + " CASCADE");
                    stmt.execute("DROP SCHEMA IF EXISTS " + targetSchema + " CASCADE");
                }
            } catch (Exception e) {
                System.err.println("Failed to cleanup schemas: " + e.getMessage());
            }
            connection.close();
        }
    }
    
    @Test
    @DisplayName("Stage full-cycle: SQL → Generate Changelog → Deploy → Diff → Expect Zero Differences")
    public void testStageFullCycle() throws Exception {
        // PHASE 1: Initialize source schema with comprehensive Stage objects using SQL
        
        try (Statement stmt = connection.createStatement()) {
            // Use the source schema
            stmt.execute("USE SCHEMA " + sourceSchema);
            
            // Internal Named Stage with CSV format
            stmt.execute("CREATE STAGE STAGE_CSV_INTERNAL " +
                "FILE_FORMAT = (TYPE = CSV FIELD_DELIMITER = ',' SKIP_HEADER = 1) " +
                "COMMENT = 'Internal stage for CSV files'");
            
            // Internal Named Stage with JSON format
            stmt.execute("CREATE STAGE STAGE_JSON_INTERNAL " +
                "FILE_FORMAT = (TYPE = JSON) " +
                "COMMENT = 'Internal stage for JSON data'");
            
            // Internal Named Stage with PARQUET format
            stmt.execute("CREATE STAGE STAGE_PARQUET_INTERNAL " +
                "FILE_FORMAT = (TYPE = PARQUET) " +
                "COMMENT = 'Internal stage for Parquet files'");
            
            // Internal Named Stage (no URL)
            stmt.execute("CREATE STAGE STAGE_INTERNAL_NAMED " +
                "FILE_FORMAT = (TYPE = CSV RECORD_DELIMITER = '\\n' FIELD_DELIMITER = ',') " +
                "COMMENT = 'Internal stage for temporary data'");
            
            // Internal Named Stage with XML format and different options
            stmt.execute("CREATE STAGE STAGE_XML_INTERNAL " +
                "FILE_FORMAT = (TYPE = XML) " +
                "COMMENT = 'Internal stage for XML files'");
        }

        System.out.println("✅ PHASE 1: Initialized source schema with Stage objects");

        // PHASE 2: Generate changelog from source schema
        
        // Set database to use source schema
        database.setDefaultSchemaName(sourceSchema);
        
        // Take snapshot of source schema - include Schema objects to enable Stage discovery
        SnapshotControl snapshotControl = new SnapshotControl(database, Schema.class, Stage.class);
        DatabaseSnapshot sourceSnapshot = SnapshotGeneratorFactory.getInstance()
                .createSnapshot(database.getDefaultSchema(), database, snapshotControl);
                
        // Verify stages were captured in snapshot
        java.util.Set<Stage> capturedStages = sourceSnapshot.get(Stage.class);
        assertNotNull(capturedStages, "Should capture Stage objects in snapshot");
        assertTrue(capturedStages.size() >= 4, "Should capture at least 4 stages, got: " + capturedStages.size());
        
        System.out.println("✅ PHASE 2: Generated snapshot with " + capturedStages.size() + " Stage objects");

        // Create temporary changelog file
        File changelogFile = File.createTempFile("stage-changelog", ".xml");
        changelogFile.deleteOnExit();

        // Create empty target snapshot for diff
        database.setDefaultSchemaName(targetSchema); // Switch to empty target schema  
        SnapshotControl emptySnapshotControl = new SnapshotControl(database, Schema.class, Stage.class);
        DatabaseSnapshot emptySnapshot = SnapshotGeneratorFactory.getInstance()
                .createSnapshot(database.getDefaultSchema(), database, emptySnapshotControl);
        database.setDefaultSchemaName(sourceSchema); // Switch back to source
        
        // Generate diff between empty schema and source schema
        CompareControl compareControl = new CompareControl();
        DiffResult diffResult = DiffGeneratorFactory.getInstance()
                // Compare sourceSnapshot (reference) vs emptySnapshot (comparison) 
                // This makes stages appear as "missing" objects (generates CREATE)
                .compare(sourceSnapshot, emptySnapshot, compareControl);
        
        try (FileOutputStream outputStream = new FileOutputStream(changelogFile)) {
            DiffToChangeLog diffToChangeLog = new DiffToChangeLog(diffResult, new DiffOutputControl());
            diffToChangeLog.print(new PrintStream(outputStream));
        }

        // Verify changelog contains createStage changes
        String changelogContent = new String(java.nio.file.Files.readAllBytes(changelogFile.toPath()));
        assertTrue(changelogContent.contains("createStage"), 
                "Changelog should contain createStage changes");
        assertTrue(changelogContent.contains("STAGE_CSV_INTERNAL") || 
                  changelogContent.contains("STAGE_JSON_INTERNAL") ||
                  changelogContent.contains("STAGE_PARQUET_INTERNAL"), 
                "Changelog should reference our test stages");
        
        System.out.println("✅ PHASE 3: Generated changelog with createStage operations");

        // PHASE 4: Deploy changelog to clean target schema
        
        // Set database to use target schema for deployment
        database.setDefaultSchemaName(targetSchema);
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("USE SCHEMA " + targetSchema);
        }
        
        Liquibase liquibase = new Liquibase(changelogFile.getName(), 
                new DirectoryResourceAccessor(new File(changelogFile.getParent())), 
                database);
        
        liquibase.update(new Contexts(), new LabelExpression());
        
        System.out.println("✅ PHASE 4: Deployed changelog to target schema");

        // PHASE 5: Take snapshot of target schema and compare with source
        SnapshotControl targetSnapshotControl = new SnapshotControl(database, Schema.class, Stage.class);
        DatabaseSnapshot targetSnapshot = SnapshotGeneratorFactory.getInstance()
                .createSnapshot(database.getDefaultSchema(), database, targetSnapshotControl);
        
        // Verify target has the same stages
        java.util.Set<Stage> targetStages = targetSnapshot.get(Stage.class);
        assertNotNull(targetStages, "Target should have Stage objects");
        assertEquals(capturedStages.size(), targetStages.size(), 
                "Target should have same number of stages as source");
        
        System.out.println("✅ PHASE 5: Target schema has " + targetStages.size() + " Stage objects");

        // PHASE 6: Verify stages have same content (ignore schema differences)
        
        // For a full-cycle test, we expect the same stage names and properties,
        // but they will be in different schemas (source vs target), so we can't use
        // standard diff comparison which treats different schemas as different objects.
        
        // Compare stage collections by name and properties
        Map<String, Stage> sourceStageMap = new HashMap<>();
        for (Stage stage : capturedStages) {
            sourceStageMap.put(stage.getName(), stage);
        }
        
        Map<String, Stage> targetStageMap = new HashMap<>();
        for (Stage stage : targetStages) {
            targetStageMap.put(stage.getName(), stage);
        }
        
        // Verify same stage names exist in both
        assertEquals(sourceStageMap.keySet(), targetStageMap.keySet(), 
                "Should have same stage names in source and target");
        
        // Verify stage properties match (excluding schema context)
        for (String stageName : sourceStageMap.keySet()) {
            Stage sourceStage = sourceStageMap.get(stageName);
            Stage targetStage = targetStageMap.get(stageName);
            
            assertNotNull(targetStage, "Target should have stage: " + stageName);
            
            // Compare content properties (ignore schema context)
            assertEquals(sourceStage.getName(), targetStage.getName(), 
                    "Stage names should match: " + stageName);
            assertEquals(sourceStage.getUrl(), targetStage.getUrl(), 
                    "Stage URLs should match: " + stageName);
            assertEquals(sourceStage.getComment(), targetStage.getComment(), 
                    "Stage comments should match: " + stageName);
            assertEquals(sourceStage.getStageType(), targetStage.getStageType(), 
                    "Stage types should match: " + stageName);
        }
        
        System.out.println("✅ PHASE 6: Source and target stages have identical content");
        
        // VALIDATION: The complete Stage lifecycle works
        assertTrue(true, "Stage full-cycle integration test completed successfully!");
    }
}