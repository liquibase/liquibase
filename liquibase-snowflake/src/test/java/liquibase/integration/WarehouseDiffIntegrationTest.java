package liquibase.integration;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.database.object.Account;
import liquibase.database.object.Warehouse;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.util.TestDatabaseConfigUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Set;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to verify diff functionality works properly with 
 * Account → Warehouse architecture.
 */
@DisplayName("Warehouse Diff Integration Tests")
public class WarehouseDiffIntegrationTest {
    
    private Database database;
    private Connection rawConnection;
    private String testWarehouse1;
    private String testWarehouse2;
    
    @BeforeEach
    void setUp() throws Exception {
        testWarehouse1 = "TEST_DIFF_WH_1";
        testWarehouse2 = "TEST_DIFF_WH_2";
        
        try {
            rawConnection = TestDatabaseConfigUtil.getSnowflakeConnection();
            JdbcConnection jdbcConnection = new JdbcConnection(rawConnection);
            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConnection);
            
            assertTrue(database instanceof SnowflakeDatabase, "Must be Snowflake database");
            
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "Cannot connect to Snowflake: " + e.getMessage());
        }
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (rawConnection != null && !rawConnection.isClosed()) {
            try (Statement stmt = rawConnection.createStatement()) {
                stmt.execute("DROP WAREHOUSE IF EXISTS " + testWarehouse1);
                stmt.execute("DROP WAREHOUSE IF EXISTS " + testWarehouse2);
                System.out.println("Cleaned up test warehouses");
            } catch (Exception e) {
                System.err.println("Failed to cleanup warehouses: " + e.getMessage());
            }
            rawConnection.close();
        }
    }
    
    @Test
    @DisplayName("Should detect added warehouses in diff")
    void testDiffDetectsAddedWarehouses() throws Exception {
        // Take initial snapshot (no test warehouses)
        SnapshotControl snapshotControl = new SnapshotControl(database, Account.class, Warehouse.class);
        DatabaseSnapshot beforeSnapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, snapshotControl);
        
        // Create test warehouses
        try (Statement stmt = rawConnection.createStatement()) {
            stmt.execute("CREATE WAREHOUSE " + testWarehouse1 + 
                       " WITH WAREHOUSE_SIZE = 'SMALL' " +
                       "AUTO_SUSPEND = 300 " +
                       "COMMENT = 'Test warehouse 1'");
            
            stmt.execute("CREATE WAREHOUSE " + testWarehouse2 + 
                       " WITH WAREHOUSE_SIZE = 'MEDIUM' " +
                       "AUTO_SUSPEND = 600 " +
                       "COMMENT = 'Test warehouse 2'");
            
            System.out.println("Created test warehouses for diff testing");
        }
        
        // Take snapshot after creating warehouses
        DatabaseSnapshot afterSnapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, snapshotControl);
        
        // Compare snapshots to detect differences
        CompareControl compareControl = new CompareControl();
        DiffResult diffResult = DiffGeneratorFactory.getInstance()
            .compare(beforeSnapshot, afterSnapshot, compareControl);
        
        // Verify that added warehouses are detected
        assertNotNull(diffResult, "Diff result should not be null");
        
        // Check for added warehouses within Account objects
        Set<Account> changedAccounts = diffResult.getChangedObjects(Account.class).keySet();
        assertNotNull(changedAccounts, "Should detect changed accounts");
        assertFalse(changedAccounts.isEmpty(), "Should have at least one changed account");
        
        System.out.println("Diff detected " + changedAccounts.size() + " changed accounts");
        
        // Find added warehouses from account changes
        boolean foundTestWh1 = false;
        boolean foundTestWh2 = false;
        
        for (Account account : changedAccounts) {
            ObjectDifferences accountDiffs = diffResult.getChangedObjects(Account.class).get(account);
            if (accountDiffs != null && accountDiffs.hasDifferences()) {
                // Check for added warehouses in the differences
                Object addedWarehouses = accountDiffs.getDifference("addedWarehouses").getReferenceValue();
                if (addedWarehouses instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Warehouse> addedWarehouseList = (List<Warehouse>) addedWarehouses;
                    
                    System.out.println("Found " + addedWarehouseList.size() + " added warehouses in account " + account.getName());
                    
                    for (Warehouse wh : addedWarehouseList) {
                        System.out.println("  Added warehouse: " + wh.getName());
                        if (testWarehouse1.equals(wh.getName())) {
                            foundTestWh1 = true;
                        }
                        if (testWarehouse2.equals(wh.getName())) {
                            foundTestWh2 = true;
                        }
                    }
                }
            }
        }
        
        assertTrue(foundTestWh1, "Should detect " + testWarehouse1 + " as added");
        assertTrue(foundTestWh2, "Should detect " + testWarehouse2 + " as added");
        
        System.out.println("✅ Diff functionality correctly detected added warehouses");
    }
    
    @Test 
    @DisplayName("Should detect modified warehouse properties in diff")
    void testDiffDetectsModifiedWarehouses() throws Exception {
        // Create initial warehouse
        try (Statement stmt = rawConnection.createStatement()) {
            stmt.execute("CREATE WAREHOUSE " + testWarehouse1 + 
                       " WITH WAREHOUSE_SIZE = 'SMALL' " +
                       "AUTO_SUSPEND = 300 " +
                       "COMMENT = 'Original comment'");
            System.out.println("Created initial warehouse for modification test");
        }
        
        // Take before snapshot
        SnapshotControl snapshotControl = new SnapshotControl(database, Account.class, Warehouse.class);
        DatabaseSnapshot beforeSnapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, snapshotControl);
        
        // Modify the warehouse
        try (Statement stmt = rawConnection.createStatement()) {
            stmt.execute("ALTER WAREHOUSE " + testWarehouse1 + 
                       " SET WAREHOUSE_SIZE = 'MEDIUM' " +
                       "AUTO_SUSPEND = 600 " +
                       "COMMENT = 'Modified comment'");
            System.out.println("Modified warehouse properties");
        }
        
        // Take after snapshot  
        DatabaseSnapshot afterSnapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, snapshotControl);
        
        // Compare snapshots
        CompareControl compareControl = new CompareControl();
        DiffResult diffResult = DiffGeneratorFactory.getInstance()
            .compare(beforeSnapshot, afterSnapshot, compareControl);
        
        // Verify modified warehouses are detected
        assertNotNull(diffResult, "Diff result should not be null");
        
        // Check for modified warehouses within Account objects
        Set<Account> changedAccounts = diffResult.getChangedObjects(Account.class).keySet();
        assertNotNull(changedAccounts, "Should detect changed accounts");
        assertFalse(changedAccounts.isEmpty(), "Should have at least one changed account");
        
        // Find modified warehouses from account changes
        boolean foundModifiedWarehouse = false;
        
        for (Account account : changedAccounts) {
            ObjectDifferences accountDiffs = diffResult.getChangedObjects(Account.class).get(account);
            if (accountDiffs != null && accountDiffs.hasDifferences()) {
                // Check for modified warehouses in the differences
                Object modifiedWarehouses = accountDiffs.getDifference("modifiedWarehouses");
                if (modifiedWarehouses != null) {
                    Object modifiedWarehouseValue = accountDiffs.getDifference("modifiedWarehouses").getReferenceValue();
                    if (modifiedWarehouseValue instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Warehouse> modifiedWarehouseList = (List<Warehouse>) modifiedWarehouseValue;
                        
                        System.out.println("Found " + modifiedWarehouseList.size() + " modified warehouses in account " + account.getName());
                        
                        for (Warehouse wh : modifiedWarehouseList) {
                            System.out.println("  Modified warehouse: " + wh.getName());
                            if (testWarehouse1.equals(wh.getName())) {
                                foundModifiedWarehouse = true;
                            }
                        }
                    }
                }
            }
        }
            
        assertTrue(foundModifiedWarehouse, "Should detect " + testWarehouse1 + " as modified");
        
        System.out.println("✅ Diff functionality correctly detected modified warehouse properties");
    }
}