package liquibase.integration;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.SnowflakeExtensionDiffGeneratorSimple;
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
import liquibase.structure.DatabaseObject;
import liquibase.util.TestDatabaseConfigUtil;
import org.junit.jupiter.api.AfterEach;
import liquibase.database.object.Account;
import liquibase.database.object.Warehouse;

import java.util.Set;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Warehouse Snapshot and Diff Integration Test:
 * 
 * Tests the correct user workflow for account-level objects (Warehouses):
 * 1. Take baseline snapshot of existing warehouses  
 * 2. Create test warehouses directly in Snowflake
 * 3. Take new snapshot showing warehouse discovery
 * 4. Diff snapshots to validate warehouse comparison
 * 5. Validate warehouse properties are captured correctly
 * 6. Cleanup test warehouses
 *
 * Note: Unlike schema objects, warehouses are infrastructure resources,
 * not deployment artifacts. This tests discovery/snapshotting, not deployment.
 */
@DisplayName("Warehouse Full-Cycle Integration Test")
public class WarehouseFullCycleIntegrationTest {

    private Database database;
    private Connection connection;
    // Warehouse isolation: Use unique names per test run to enable parallel execution
    private String testId = String.valueOf(System.currentTimeMillis());
    private String testWarehouse1 = "WH_FC_SMALL_" + testId;
    private String testWarehouse2 = "WH_FC_MEDIUM_" + testId;  
    private String testWarehouse3 = "WH_FC_LARGE_" + testId;
    private String testWarehouse4 = "WH_FC_XLARGE_" + testId;
    
    @BeforeEach
    public void setUp() throws Exception {
        try {
            connection = TestDatabaseConfigUtil.getSnowflakeConnection();
            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            
            // Clean up any existing test warehouses
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP WAREHOUSE IF EXISTS " + testWarehouse1);
                stmt.execute("DROP WAREHOUSE IF EXISTS " + testWarehouse2);
                stmt.execute("DROP WAREHOUSE IF EXISTS " + testWarehouse3);
                stmt.execute("DROP WAREHOUSE IF EXISTS " + testWarehouse4);
            }
            
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "Cannot connect to Snowflake: " + e.getMessage());
        }
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP WAREHOUSE IF EXISTS " + testWarehouse1);
                stmt.execute("DROP WAREHOUSE IF EXISTS " + testWarehouse2);
                stmt.execute("DROP WAREHOUSE IF EXISTS " + testWarehouse3);
                stmt.execute("DROP WAREHOUSE IF EXISTS " + testWarehouse4);
            } catch (Exception e) {
                System.err.println("Failed to cleanup warehouses: " + e.getMessage());
            }
            connection.close();
        }
    }
    
    @Test
    @DisplayName("Warehouse snapshot and diff test: Baseline → Create → Snapshot → Diff → Validate")
    public void testWarehouseSnapshotAndDiff() throws Exception {
        // PHASE 1: Initialize with comprehensive Warehouse objects using SQL
        
        try (Statement stmt = connection.createStatement()) {
            
            // Small warehouse with basic configuration
            stmt.execute("CREATE WAREHOUSE " + testWarehouse1 + " " +
                "WAREHOUSE_SIZE = 'SMALL' " +
                "AUTO_SUSPEND = 300 " +
                "AUTO_RESUME = TRUE " +
                "MIN_CLUSTER_COUNT = 1 " +
                "MAX_CLUSTER_COUNT = 1 " +
                "SCALING_POLICY = 'STANDARD' " +
                "INITIALLY_SUSPENDED = FALSE");
            
            // Medium warehouse with resource monitoring
            stmt.execute("CREATE WAREHOUSE " + testWarehouse2 + " " +
                "WAREHOUSE_SIZE = 'MEDIUM' " +
                "AUTO_SUSPEND = 600 " +
                "AUTO_RESUME = TRUE " +
                "MIN_CLUSTER_COUNT = 1 " +
                "MAX_CLUSTER_COUNT = 3 " +
                "SCALING_POLICY = 'ECONOMY' " +
                "INITIALLY_SUSPENDED = TRUE " +
                "RESOURCE_MONITOR = NULL");
            
            // Large warehouse with multi-cluster configuration
            stmt.execute("CREATE WAREHOUSE " + testWarehouse3 + " " +
                "WAREHOUSE_SIZE = 'LARGE' " +
                "AUTO_SUSPEND = 900 " +
                "AUTO_RESUME = FALSE " +
                "MIN_CLUSTER_COUNT = 2 " +
                "MAX_CLUSTER_COUNT = 5 " +
                "SCALING_POLICY = 'STANDARD' " +
                "INITIALLY_SUSPENDED = FALSE");
            
            // X-Large warehouse with advanced settings
            stmt.execute("CREATE WAREHOUSE " + testWarehouse4 + " " +
                "WAREHOUSE_SIZE = 'XLARGE' " +
                "AUTO_SUSPEND = 60 " +
                "AUTO_RESUME = TRUE " +
                "MIN_CLUSTER_COUNT = 1 " +
                "MAX_CLUSTER_COUNT = 10 " +
                "SCALING_POLICY = 'ECONOMY' " +
                "INITIALLY_SUSPENDED = TRUE");
            
            
            // DEBUG: Verify test warehouses exist immediately after creation
            try (Statement verifyStmt = connection.createStatement()) {
                ResultSet rs = verifyStmt.executeQuery("SHOW WAREHOUSES LIKE 'WH_FULL_CYCLE_%'");
                int count = 0;
                while (rs.next()) {
                    count++;
                    String name = rs.getString("name");
                    String size = rs.getString("size");
                }
                if (count != 4) {
                }
                rs.close();
            } catch (Exception e) {
            }
        }
        
        // PHASE 2: Take initial snapshot and generate changelog
        
        // Take snapshot including Account and Warehouse objects
        // CRITICAL FIX: Account-level objects don't belong to a schema context
        SnapshotControl snapshotControl = new SnapshotControl(database, Account.class, Warehouse.class);
        
        // For account-level objects, we need to provide an Account object as the root
        // Create an Account object to serve as the root for warehouse discovery
        Account rootAccount = new Account();
        rootAccount.setName(database.getConnection().getURL()); // Use connection URL as account identifier
        
        DatabaseSnapshot initialSnapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(new DatabaseObject[]{rootAccount}, database, snapshotControl);
        
        // DEBUG: Check what's in the initial snapshot
        Set<Warehouse> warehouses = initialSnapshot.get(Warehouse.class);
        
        // DEBUG: Check warehouses stored in Account objects
        Set<Account> accounts = initialSnapshot.get(Account.class);
        if (accounts != null) {
            for (Account account : accounts) {
                for (Warehouse wh : account.getDatabaseObjects(Warehouse.class)) {
                    String isTestWarehouse = wh.getName().startsWith("WH_FULL_CYCLE_") ? " [TEST WAREHOUSE]" : "";
                }
            }
        }
        
        // CRITICAL DEBUG: Check if warehouses are accessible at top-level for diff
        Set<Warehouse> topLevelWarehouses = initialSnapshot.get(Warehouse.class);
        if (topLevelWarehouses != null && !topLevelWarehouses.isEmpty()) {
            for (Warehouse wh : topLevelWarehouses) {
            }
        }
        
        // Generate changelog by creating empty snapshot and comparing
        CompareControl compareControl = new CompareControl();
        
        // CRITICAL FIX: Create truly empty snapshot that doesn't auto-discover warehouses
        // The problem is that even Schema-only snapshots trigger warehouse discovery via Account addsTo()
        // Solution: Use a custom SnapshotControl that explicitly excludes warehouse discovery
        SnapshotControl emptyControl = new SnapshotControl(database) {
            @Override
            public boolean shouldInclude(Class<? extends DatabaseObject> type) {
                // Explicitly exclude Account and Warehouse objects to prevent auto-discovery
                if (Account.class.isAssignableFrom(type) || Warehouse.class.isAssignableFrom(type)) {
                    return false;
                }
                // Only include basic schema-level objects (not account-level)
                return Schema.class.isAssignableFrom(type);
            }
        };
        
        DatabaseSnapshot emptySnapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(new DatabaseObject[0], database, emptyControl);
            
        // DEBUG: Check what the empty snapshot actually contains
        Set<Warehouse> emptyWarehouses = emptySnapshot.get(Warehouse.class);
        if (emptyWarehouses != null && !emptyWarehouses.isEmpty()) {
            for (Warehouse wh : emptyWarehouses) {
            }
        }
        
        // DEBUG: Double-check what's in initial snapshot right before diff
        Set<Warehouse> initialWarehouses = initialSnapshot.get(Warehouse.class);
        if (initialWarehouses != null && !initialWarehouses.isEmpty()) {
            for (Warehouse wh : initialWarehouses) {
                if (wh.getName().startsWith("WH_FULL_CYCLE_")) {
                }
            }
        }
        
        
        // CRITICAL FIX: Use SnowflakeExtensionDiffGeneratorSimple directly for account-level objects
        // DiffGeneratorFactory doesn't properly handle extension → extension relationships for Warehouses
        // This makes warehouses appear as "missing" objects (generates CREATE), not "unexpected" objects (generates DROP)
        
        SnowflakeExtensionDiffGeneratorSimple extensionDiff = new SnowflakeExtensionDiffGeneratorSimple();
        DiffResult fullDiffResult = extensionDiff.compare(initialSnapshot, emptySnapshot, compareControl);
        
        // DEBUG: Show what's in the full diff result
        System.out.println("DEBUG: Full diff result has " + fullDiffResult.getMissingObjects().size() + " missing objects and " + fullDiffResult.getUnexpectedObjects().size() + " unexpected objects");
        for (DatabaseObject obj : fullDiffResult.getMissingObjects()) {
            if (obj instanceof Warehouse) {
                System.out.println("DEBUG: Missing warehouse: " + obj.getName());
            }
        }
        for (DatabaseObject obj : fullDiffResult.getUnexpectedObjects()) {
            if (obj instanceof Warehouse) {
                System.out.println("DEBUG: Unexpected warehouse: " + obj.getName());
            }
        }
        
        // CRITICAL FIX: Filter diff result to include only test warehouses to avoid COMPUTE_WH isolation issue
        // Create a new DiffResult that only contains test warehouses (names starting with "WH_FULL_CYCLE_")
        DiffResult filteredDiffResult = new DiffResult(emptySnapshot, initialSnapshot, compareControl);
        
        // Check both unexpected (in emptySnapshot but not initialSnapshot - for CREATE) and missing (in initialSnapshot but not emptySnapshot - for DROP)
        for (DatabaseObject obj : fullDiffResult.getUnexpectedObjects()) {
            if (obj instanceof Warehouse && obj.getName().startsWith("WH_FC_")) {
                filteredDiffResult.addUnexpectedObject(obj);
            }
        }
        
        for (DatabaseObject obj : fullDiffResult.getMissingObjects()) {
            if (obj instanceof Warehouse && obj.getName().startsWith("WH_FC_")) {
                filteredDiffResult.addMissingObject(obj);
            }
        }
        
        // DEBUG: Check specifically for our test warehouses in the filtered diff (missing = CREATE statements)
        int testWarehouseCount = 0;
        for (DatabaseObject obj : filteredDiffResult.getMissingObjects()) {
            if (obj instanceof Warehouse && obj.getName().startsWith("WH_FC_")) {
                testWarehouseCount++;
            }
        }
        
        // Write changelog to temporary file
        File tempChangelogFile = File.createTempFile("warehouse-fullcycle-", ".xml");
        tempChangelogFile.deleteOnExit();
        
        try (FileOutputStream outputStream = new FileOutputStream(tempChangelogFile)) {
            DiffToChangeLog diffToChangeLog = new DiffToChangeLog(filteredDiffResult, new DiffOutputControl());
            diffToChangeLog.print(new PrintStream(outputStream));
        }
        
        
        // DEBUG: Always show changelog contents
        try {
            java.util.List<String> lines = java.nio.file.Files.readAllLines(tempChangelogFile.toPath());
            for (String line : lines) {
                if (lines.indexOf(line) > 50) { // Limit to first 50 lines
                    break;
                }
            }
        } catch (Exception e) {
        }
        
        // PHASE 3: Drop all warehouses to simulate clean state
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP WAREHOUSE IF EXISTS " + testWarehouse1);
            stmt.execute("DROP WAREHOUSE IF EXISTS " + testWarehouse2);
            stmt.execute("DROP WAREHOUSE IF EXISTS " + testWarehouse3);
            stmt.execute("DROP WAREHOUSE IF EXISTS " + testWarehouse4);
        }
        
        // Verify warehouses are gone by taking new snapshot
        DatabaseSnapshot droppedSnapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(new DatabaseObject[]{rootAccount}, database, snapshotControl);
        
        
        // PHASE 4: Deploy generated changelog to recreate warehouses
        
        // CRITICAL FIX: Set an active warehouse for changelog table creation
        // Since we dropped all test warehouses, we need a warehouse for compute
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("USE WAREHOUSE LTHDB_TEST_WH"); // Use the default test warehouse
        } catch (Exception e) {
            System.err.println("Warning: Could not set active warehouse: " + e.getMessage());
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
        DatabaseSnapshot finalSnapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(new DatabaseObject[]{rootAccount}, database, snapshotControl);
        
        // Compare initial snapshot with final snapshot
        CompareControl finalCompareControl = new CompareControl();
        // Suppress timestamp fields that might vary
        finalCompareControl.addSuppressedField(Warehouse.class, "createdOn");
        finalCompareControl.addSuppressedField(Warehouse.class, "resumedOn");
        finalCompareControl.addSuppressedField(Warehouse.class, "updatedOn");
        
        DiffResult finalDiff = DiffGeneratorFactory.getInstance()
            .compare(initialSnapshot, finalSnapshot, finalCompareControl);
        
        // VALIDATION: Assert no differences
        if (!finalDiff.areEqual()) {
            System.err.println("❌ FOUND DIFFERENCES - Full cycle failed!");
            System.err.println("Missing objects: " + finalDiff.getMissingObjects().size());
            System.err.println("Unexpected objects: " + finalDiff.getUnexpectedObjects().size());
            System.err.println("Changed objects: " + finalDiff.getChangedObjects().size());
            
            // Print detailed diff for debugging
            new DiffToReport(finalDiff, System.err).print();
            
            fail("Full-cycle test failed: Initial and final warehouse states are not identical");
        }
        
        
        // Additional validation: Verify specific warehouse properties are preserved
        boolean found1 = false, found2 = false, found3 = false, found4 = false;
        
        int accountCount = finalSnapshot.get(Account.class).size();
        
        // Check both top-level and account-level warehouses for our test warehouses
        Set<Warehouse> finalTopLevelWarehouses = finalSnapshot.get(Warehouse.class);
        if (finalTopLevelWarehouses != null) {
            for (Warehouse wh : finalTopLevelWarehouses) {
                if (wh.getName().equals(testWarehouse1)) {
                    found1 = true;
                    assertEquals("SMALL", wh.getSize());
                    assertEquals(Integer.valueOf(300), wh.getAutoSuspend());
                } else if (wh.getName().equals(testWarehouse2)) {
                    found2 = true;
                    assertEquals("MEDIUM", wh.getSize());
                    assertEquals(Integer.valueOf(600), wh.getAutoSuspend());
                } else if (wh.getName().equals(testWarehouse3)) {
                    found3 = true;
                    assertEquals("LARGE", wh.getSize());
                    assertEquals(Integer.valueOf(900), wh.getAutoSuspend());
                } else if (wh.getName().equals(testWarehouse4)) {
                    found4 = true;
                    assertEquals("XLARGE", wh.getSize());
                    assertEquals(Integer.valueOf(60), wh.getAutoSuspend());
                }
            }
        }
        
        // Also check account-level warehouses
        for (Account account : finalSnapshot.get(Account.class)) {
            for (Object obj : account.getDatabaseObjects()) {
                if (obj instanceof Warehouse) {
                    Warehouse wh = (Warehouse) obj;
                    if (wh.getName().equals(testWarehouse1) && !found1) {
                        found1 = true;
                        assertEquals("SMALL", wh.getSize());
                        assertEquals(Integer.valueOf(300), wh.getAutoSuspend());
                    } else if (wh.getName().equals(testWarehouse2) && !found2) {
                        found2 = true;
                        assertEquals("MEDIUM", wh.getSize());
                        assertEquals(Integer.valueOf(600), wh.getAutoSuspend());
                    } else if (wh.getName().equals(testWarehouse3) && !found3) {
                        found3 = true;
                        assertEquals("LARGE", wh.getSize());
                        assertEquals(Integer.valueOf(900), wh.getAutoSuspend());
                    } else if (wh.getName().equals(testWarehouse4) && !found4) {
                        found4 = true;
                        assertEquals("XLARGE", wh.getSize());
                        assertEquals(Integer.valueOf(60), wh.getAutoSuspend());
                    }
                }
            }
        }
        
        // If warehouses are missing, provide detailed error message about what was actually found
        if (!(found1 && found2 && found3 && found4)) {
            StringBuilder errorMsg = new StringBuilder("Expected test warehouses not found. ");
            errorMsg.append("Missing: ");
            if (!found1) errorMsg.append(testWarehouse1).append(" ");
            if (!found2) errorMsg.append(testWarehouse2).append(" ");
            if (!found3) errorMsg.append(testWarehouse3).append(" ");
            if (!found4) errorMsg.append(testWarehouse4).append(" ");
            
            errorMsg.append("\nActual warehouses found: ");
            if (finalTopLevelWarehouses != null) {
                for (Warehouse wh : finalTopLevelWarehouses) {
                    errorMsg.append(wh.getName()).append(" ");
                }
            }
            
            fail(errorMsg.toString());
        }
        
        
        // Cleanup temp file
        tempChangelogFile.delete();
    }
}