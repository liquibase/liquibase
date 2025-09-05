package liquibase.integration;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.SnowflakeExtensionDiffGeneratorSimple;
import liquibase.diff.compare.CompareControl;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.database.object.Account;
import liquibase.database.object.Warehouse;
import liquibase.util.TestDatabaseConfigUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Set;

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
@DisplayName("Warehouse Snapshot and Diff Integration Test")
public class WarehouseSnapshotAndDiffIntegrationTest {

    private Database database;
    private Connection connection;
    private String testWarehouse1 = "WH_SNAPSHOT_TEST_SMALL";
    private String testWarehouse2 = "WH_SNAPSHOT_TEST_MEDIUM";  
    private String testWarehouse3 = "WH_SNAPSHOT_TEST_LARGE";
    private String testWarehouse4 = "WH_SNAPSHOT_TEST_XLARGE";
    
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
            // Clean up test warehouses
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP WAREHOUSE IF EXISTS " + testWarehouse1);
                stmt.execute("DROP WAREHOUSE IF EXISTS " + testWarehouse2);
                stmt.execute("DROP WAREHOUSE IF EXISTS " + testWarehouse3);
                stmt.execute("DROP WAREHOUSE IF EXISTS " + testWarehouse4);
            } catch (Exception e) {
                // Ignore cleanup failures
            }
            
            connection.close();
        }
    }
    
    @Test
    @DisplayName("Warehouse snapshot and diff test: Baseline → Create → Snapshot → Diff → Validate")
    public void testWarehouseSnapshotAndDiff() throws Exception {
        // PHASE 1: Take baseline snapshot of existing warehouses
        SnapshotControl snapshotControl = new SnapshotControl(database);
        DatabaseSnapshot baselineSnapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(new DatabaseObject[0], database, snapshotControl);
        
        Set<Warehouse> baselineWarehouses = baselineSnapshot.get(Warehouse.class);
        int baselineCount = baselineWarehouses != null ? baselineWarehouses.size() : 0;
        
        // Count existing test warehouses (should be 0 after cleanup)
        int baselineTestCount = 0;
        if (baselineWarehouses != null) {
            for (Warehouse wh : baselineWarehouses) {
                if (wh.getName() != null && wh.getName().startsWith("WH_SNAPSHOT_TEST_")) {
                    baselineTestCount++;
                }
            }
        }
        
        // PHASE 2: Create test warehouses directly in Snowflake (infrastructure operation)
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
                "INITIALLY_SUSPENDED = TRUE");
            
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
        } catch (Exception e) {
            fail("Failed to create test warehouses: " + e.getMessage());
        }
        
        // PHASE 3: Take new snapshot showing warehouse discovery 
        DatabaseSnapshot newSnapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(new DatabaseObject[0], database, snapshotControl);
        
        Set<Warehouse> newWarehouses = newSnapshot.get(Warehouse.class);
        int newCount = newWarehouses != null ? newWarehouses.size() : 0;
        
        // Count our test warehouses in the new snapshot
        int newTestCount = 0;
        if (newWarehouses != null) {
            for (Warehouse wh : newWarehouses) {
                if (wh.getName() != null && wh.getName().startsWith("WH_SNAPSHOT_TEST_")) {
                    newTestCount++;
                }
            }
        }
        
        // VALIDATION 1: New snapshot should discover our 4 test warehouses
        assertEquals(baselineTestCount + 4, newTestCount, 
            "Should find exactly 4 test warehouses (baseline: " + baselineTestCount + 
            ", found: " + newTestCount + ")");        
        // PHASE 4: Diff snapshots to validate warehouse comparison
        CompareControl compareControl = new CompareControl();
        // Suppress timestamp fields that can vary
        compareControl.addSuppressedField(Warehouse.class, "createdOn");  
        compareControl.addSuppressedField(Warehouse.class, "resumedOn");
        compareControl.addSuppressedField(Warehouse.class, "updatedOn");
        
        // Use SnowflakeExtensionDiffGeneratorSimple directly for account-level objects
        // DiffGeneratorFactory doesn't properly handle extension → extension relationships for Warehouses
        SnowflakeExtensionDiffGeneratorSimple extensionDiff = new SnowflakeExtensionDiffGeneratorSimple();
        DiffResult diffResult = extensionDiff.compare(baselineSnapshot, newSnapshot, compareControl);
        
        // VALIDATION 2: Diff should show 4 new warehouses as "unexpected" objects
        int testWarehouseCount = 0;
        for (DatabaseObject obj : diffResult.getUnexpectedObjects()) {
            if (obj instanceof Warehouse && obj.getName().startsWith("WH_SNAPSHOT_TEST_")) {
                testWarehouseCount++;
            }
        }
        assertEquals(4, testWarehouseCount, "Values should be equal");        
        // PHASE 5: Validate warehouse properties are captured correctly
        boolean found1 = false, found2 = false, found3 = false, found4 = false;
        
        // Check both top-level and account-level warehouses for our test warehouses  
        Set<Warehouse> finalTopLevelWarehouses = newSnapshot.get(Warehouse.class);
        if (finalTopLevelWarehouses != null) {
            for (Warehouse wh : finalTopLevelWarehouses) {
                switch (wh.getName()) {
                    case "WH_SNAPSHOT_TEST_SMALL":
                        found1 = true;
                        assertEquals("SMALL", wh.getSize(), "Small warehouse should have SMALL size");
                        assertEquals(Integer.valueOf(300), wh.getAutoSuspend(), "Small warehouse should have 300s auto-suspend");
                        break;
                    case "WH_SNAPSHOT_TEST_MEDIUM": 
                        found2 = true;
                        assertEquals("MEDIUM", wh.getSize(), "Medium warehouse should have MEDIUM size");
                        assertEquals(Integer.valueOf(600), wh.getAutoSuspend(), "Medium warehouse should have 600s auto-suspend");
                        break;
                    case "WH_SNAPSHOT_TEST_LARGE":
                        found3 = true;
                        assertEquals("LARGE", wh.getSize(), "Large warehouse should have LARGE size");
                        assertEquals(Integer.valueOf(900), wh.getAutoSuspend(), "Large warehouse should have 900s auto-suspend");
                        break;
                    case "WH_SNAPSHOT_TEST_XLARGE":
                        found4 = true;
                        assertEquals("XLARGE", wh.getSize(), "X-Large warehouse should have XLARGE size (normalized)");
                        assertEquals(Integer.valueOf(60), wh.getAutoSuspend(), "X-Large warehouse should have 60s auto-suspend");
                        break;
                }
            }
        }
        
        // Also check account-level warehouses in case they're stored there
        Set<Account> accounts = newSnapshot.get(Account.class);
        if (accounts != null) {
            for (Account account : accounts) {
                for (Object obj : account.getDatabaseObjects()) {
                    if (obj instanceof Warehouse) {
                        Warehouse wh = (Warehouse) obj;
                        switch (wh.getName()) {
                            case "WH_SNAPSHOT_TEST_SMALL":
                                if (!found1) {
                                    found1 = true;
                                    assertEquals("SMALL", wh.getSize(), "Small warehouse should have SMALL size");
                                    assertEquals(Integer.valueOf(300), wh.getAutoSuspend(), "Small warehouse should have 300s auto-suspend");
                                }
                                break;
                            case "WH_SNAPSHOT_TEST_MEDIUM":
                                if (!found2) {
                                    found2 = true;
                                    assertEquals("MEDIUM", wh.getSize(), "Medium warehouse should have MEDIUM size");
                                    assertEquals(Integer.valueOf(600), wh.getAutoSuspend(), "Medium warehouse should have 600s auto-suspend");
                                }
                                break;
                            case "WH_SNAPSHOT_TEST_LARGE":
                                if (!found3) {
                                    found3 = true;
                                    assertEquals("LARGE", wh.getSize(), "Large warehouse should have LARGE size");
                                    assertEquals(Integer.valueOf(900), wh.getAutoSuspend(), "Large warehouse should have 900s auto-suspend");
                                }
                                break;
                            case "WH_SNAPSHOT_TEST_XLARGE":
                                if (!found4) {
                                    found4 = true;
                                    assertEquals("XLARGE", wh.getSize(), "X-Large warehouse should have XLARGE size (normalized)");
                                    assertEquals(Integer.valueOf(60), wh.getAutoSuspend(), "X-Large warehouse should have 60s auto-suspend");
                                }
                                break;
                        }
                    }
                }
            }
        }
        
        // VALIDATION 3: All test warehouses should be discovered with correct properties
        assertTrue(found1 && found2 && found3 && found4, "Assertion should be true");    }
}