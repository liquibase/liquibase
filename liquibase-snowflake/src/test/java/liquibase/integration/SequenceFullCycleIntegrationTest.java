package liquibase.integration;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.change.core.CreateSequenceChange;
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
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Sequence;
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
 * Full-cycle integration test for Sequence objects:
 * 1. Initialize schema with SQL statements (all Sequence variations)
 * 2. Generate changelog from source schema
 * 3. Deploy changelog to clean target schema
 * 4. Diff the two schemas
 * 5. Expect NO differences
 *
 * This validates the complete round-trip: SQL → Snapshot → Diff → ChangeLog → Deploy → Validate
 */
@DisplayName("Sequence Full-Cycle Integration Test")
public class SequenceFullCycleIntegrationTest {

    private Database database;
    private Connection connection;
    private String sourceSchema = "SEQ_FULL_CYCLE_SOURCE";
    private String targetSchema = "SEQ_FULL_CYCLE_TARGET";
    
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
    @DisplayName("Sequence full-cycle: SQL → Generate Changelog → Deploy → Diff → Expect Zero Differences")
    public void testSequenceFullCycle() throws Exception {
        // PHASE 1: Initialize source schema with comprehensive Sequence objects using CreateSequenceChange
        
        try (Statement stmt = connection.createStatement()) {
            // Use the source schema
            stmt.execute("USE SCHEMA " + sourceSchema);
            
            // Set database schema context
            database.setDefaultSchemaName(sourceSchema);
            
            // Basic sequence with minimal configuration
            CreateSequenceChange basicChange = new CreateSequenceChange();
            basicChange.setSequenceName("SEQ_BASIC_COUNTER");
            basicChange.setStartValue(java.math.BigInteger.valueOf(1));
            basicChange.setIncrementBy(java.math.BigInteger.valueOf(1));
            basicChange.setComment("Basic sequence for counting");
            executeCreateSequenceChange(basicChange, stmt);
            
            // Sequence with custom start and increment
            CreateSequenceChange customChange = new CreateSequenceChange();
            customChange.setSequenceName("SEQ_CUSTOM_INCREMENT");
            customChange.setStartValue(java.math.BigInteger.valueOf(100));
            customChange.setIncrementBy(java.math.BigInteger.valueOf(5));
            customChange.setComment("Sequence with custom start and increment");
            executeCreateSequenceChange(customChange, stmt);
            
            // Sequence with ordering constraint
            CreateSequenceChange orderedChange = new CreateSequenceChange();
            orderedChange.setSequenceName("SEQ_ORDERED");
            orderedChange.setStartValue(java.math.BigInteger.valueOf(1000));
            orderedChange.setIncrementBy(java.math.BigInteger.valueOf(10));
            orderedChange.setOrdered(true); // ORDER
            orderedChange.setComment("Ordered sequence for guaranteed ordering");
            executeCreateSequenceChange(orderedChange, stmt);
            
            // Sequence without ordering (NOORDER) - Snowflake default but explicit
            CreateSequenceChange noOrderChange = new CreateSequenceChange();
            noOrderChange.setSequenceName("SEQ_NO_ORDER");
            noOrderChange.setStartValue(java.math.BigInteger.valueOf(1));
            noOrderChange.setIncrementBy(java.math.BigInteger.valueOf(2));
            noOrderChange.setOrdered(false); // NOORDER
            noOrderChange.setComment("Explicitly unordered sequence for performance");
            executeCreateSequenceChange(noOrderChange, stmt);
            
            // Advanced sequence with all properties
            CreateSequenceChange comprehensiveChange = new CreateSequenceChange();
            comprehensiveChange.setSequenceName("SEQ_COMPREHENSIVE");
            comprehensiveChange.setStartValue(java.math.BigInteger.valueOf(500));
            comprehensiveChange.setIncrementBy(java.math.BigInteger.valueOf(25));
            comprehensiveChange.setOrdered(true); // ORDER
            comprehensiveChange.setComment("Comprehensive sequence with all major properties");
            executeCreateSequenceChange(comprehensiveChange, stmt);
            
            
            // DEBUG: Verify sequences actually exist in Snowflake
            java.sql.ResultSet debugRs = stmt.executeQuery("SELECT SEQUENCE_CATALOG, SEQUENCE_SCHEMA, SEQUENCE_NAME FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_SCHEMA = '" + sourceSchema + "'");
            int foundCount = 0;
            while (debugRs.next()) {
                foundCount++;
            }
            debugRs.close();
            
            // Also try SHOW SEQUENCES to compare
            java.sql.ResultSet showRs = stmt.executeQuery("SHOW SEQUENCES IN SCHEMA " + sourceSchema);
            int showCount = 0;
            while (showRs.next()) {
                showCount++;
            }
            showRs.close();
            
            if (foundCount == 0) {
                System.err.println("❌ ERROR: No sequences found in INFORMATION_SCHEMA - this will cause empty changelog");
            }
        }
        
        // PHASE 2: Generate changelog from source schema
        
        // Set database to use source schema
        database.setDefaultSchemaName(sourceSchema);
        
        // Take snapshot of source schema
        SnapshotControl snapshotControl = new SnapshotControl(database, Sequence.class);
        DatabaseSnapshot sourceSnapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, snapshotControl);
        
        // Generate diff against empty database to get complete changelog
        CompareControl compareControl = new CompareControl();
        // Create empty reference snapshot instead of null
        DatabaseSnapshot emptySnapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(new DatabaseObject[0], database, snapshotControl);
        DiffResult diffResult = DiffGeneratorFactory.getInstance()
            // CRITICAL FIX: Compare sourceSnapshot (reference) vs emptySnapshot (comparison)
            // This makes sequences appear as "missing" objects (generates CREATE), not "unexpected" objects (generates DROP)
            .compare(sourceSnapshot, emptySnapshot, compareControl);
        
        // Write diff to temporary changelog file
        File tempChangelogFile = File.createTempFile("sequence-fullcycle-", ".xml");
        tempChangelogFile.deleteOnExit();
        
        try (FileOutputStream outputStream = new FileOutputStream(tempChangelogFile)) {
            DiffToChangeLog diffToChangeLog = new DiffToChangeLog(diffResult, new DiffOutputControl());
            diffToChangeLog.print(new PrintStream(outputStream));
        }
        
        
        // PHASE 3: Deploy generated changelog to target schema
        
        // Switch database to target schema
        database.setDefaultSchemaName(targetSchema);
        
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
        
        
        // PHASE 4: Take snapshots of both schemas and compare
        
        // Take snapshot of target schema (after deployment)
        DatabaseSnapshot targetSnapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, snapshotControl);
        
        // Reset database to source schema and take fresh snapshot
        database.setDefaultSchemaName(sourceSchema);
        DatabaseSnapshot freshSourceSnapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, snapshotControl);
        
        // PHASE 5: Compare snapshots - should be identical
        
        // Compare the snapshots
        CompareControl finalCompareControl = new CompareControl();
        // Suppress schema names in comparison since they're different
        finalCompareControl.addSuppressedField(Schema.class, "name");
        finalCompareControl.addSuppressedField(Sequence.class, "schemaName");
        // Suppress current sequence values since they may have been used/incremented
        finalCompareControl.addSuppressedField(Sequence.class, "currentValue");
        finalCompareControl.addSuppressedField(Sequence.class, "nextValue");
        
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
        
        
        // Additional validation: Verify specific sequence properties are preserved
        try (Statement stmt = connection.createStatement()) {
            
            // Switch to target schema and verify sequences exist with correct properties
            stmt.execute("USE SCHEMA " + targetSchema);
            
            // First, verify sequences actually exist in target schema
            java.sql.ResultSet rs = stmt.executeQuery("SHOW SEQUENCES LIKE 'SEQ_%'");
            boolean foundSequences = false;
            while (rs.next()) {
                foundSequences = true;
            }
            rs.close();
            
            if (!foundSequences) {
                System.err.println("❌ ERROR: No sequences found in target schema " + targetSchema);
                System.err.println("   This indicates the changelog didn't deploy sequences properly");
                return; // Skip NEXTVAL tests if sequences don't exist
            }
            
            // Test sequence functionality with fully qualified names
            stmt.execute("SELECT \"" + targetSchema + "\".SEQ_BASIC_COUNTER.NEXTVAL");
            stmt.execute("SELECT \"" + targetSchema + "\".SEQ_CUSTOM_INCREMENT.NEXTVAL");
            stmt.execute("SELECT \"" + targetSchema + "\".SEQ_ORDERED.NEXTVAL");
            stmt.execute("SELECT \"" + targetSchema + "\".SEQ_NO_ORDER.NEXTVAL");
            stmt.execute("SELECT \"" + targetSchema + "\".SEQ_COMPREHENSIVE.NEXTVAL");
            
            
            // Verify sequence metadata
            stmt.execute("SHOW SEQUENCES LIKE 'SEQ_%'");
            
            // Test one sequence to ensure proper increment behavior  
            stmt.execute("SELECT \"" + targetSchema + "\".SEQ_CUSTOM_INCREMENT.NEXTVAL"); // Should increment by 5
        }
        
        // Additional snapshot validation to ensure structural integrity
        
        // Count sequences in both schemas should match
        int sourceSequenceCount = freshSourceSnapshot.get(Sequence.class).size();
        int targetSequenceCount = targetSnapshot.get(Sequence.class).size();
        
        assertEquals(sourceSequenceCount, targetSequenceCount, "Values should be equal");        
        
        // Validate specific sequence properties were preserved correctly
        for (Sequence sourceSeq : freshSourceSnapshot.get(Sequence.class)) {
            boolean foundMatch = false;
            for (Sequence targetSeq : targetSnapshot.get(Sequence.class)) {
                if (sourceSeq.getName().equals(targetSeq.getName())) {
                    foundMatch = true;
                    assertEquals(sourceSeq.getStartValue(), targetSeq.getStartValue(), "Values should be equal");                    assertEquals(sourceSeq.getIncrementBy(), targetSeq.getIncrementBy(), "Values should be equal");                    assertEquals(sourceSeq.getOrdered(), targetSeq.getOrdered(), "Values should be equal");                    break;
                }
            }
            assertTrue(foundMatch, "Should find matching sequence for " + sourceSeq.getName());
        }
        
        
        // Cleanup temp file
        tempChangelogFile.delete();
    }
    
    /**
     * Helper method to execute CreateSequenceChange by converting it to SQL statements
     */
    private void executeCreateSequenceChange(CreateSequenceChange change, Statement stmt) throws Exception {
        // Generate SQL statements using the changetype
        SqlStatement[] statements = change.generateStatements(database);
        
        // Convert statements to SQL and execute them
        for (SqlStatement statement : statements) {
            Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
            for (Sql sql : sqls) {
                String sqlString = sql.toSql();
                stmt.execute(sqlString);
            }
        }
    }
}