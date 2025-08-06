package liquibase.integration;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.database.object.Account;
import liquibase.database.object.Warehouse;
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
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for Account → Warehouse snapshot architecture.
 * Validates that warehouses are properly contained within Account objects
 * and that Account objects are peers to Catalog objects in DatabaseSnapshot.
 */
@DisplayName("Account → Warehouse Snapshot Integration Tests")
public class AccountWarehouseSnapshotIntegrationTest {
    
    private Database database;
    private Connection rawConnection;
    private String testWarehouseName;
    
    @BeforeEach
    void setUp() throws Exception {
        testWarehouseName = "TEST_ACCOUNT_WH_INTEGRATION";
        
        try {
            rawConnection = TestDatabaseConfigUtil.getSnowflakeConnection();
            JdbcConnection jdbcConnection = new JdbcConnection(rawConnection);
            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConnection);
            
            assertTrue(database instanceof SnowflakeDatabase, "Must be Snowflake database");
            
            // Create test warehouse for snapshot validation
            try (Statement stmt = rawConnection.createStatement()) {
                stmt.execute("DROP WAREHOUSE IF EXISTS " + testWarehouseName);
                stmt.execute("CREATE WAREHOUSE " + testWarehouseName + 
                           " WITH WAREHOUSE_SIZE = 'MEDIUM' " +
                           "AUTO_SUSPEND = 300 " +
                           "AUTO_RESUME = TRUE " +
                           "COMMENT = 'Test warehouse for Account integration testing'");
                System.out.println("Created test warehouse: " + testWarehouseName);
            }
            
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "Cannot connect to Snowflake or create test warehouse: " + e.getMessage());
        }
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (rawConnection != null && !rawConnection.isClosed()) {
            try (Statement stmt = rawConnection.createStatement()) {
                stmt.execute("DROP WAREHOUSE IF EXISTS " + testWarehouseName);
                System.out.println("Cleaned up test warehouse: " + testWarehouseName);
            } catch (Exception e) {
                System.err.println("Failed to cleanup warehouse: " + e.getMessage());
            }
            rawConnection.close();
        }
    }
    
    @Test
    @DisplayName("Account objects should appear in database snapshot as peers to Catalog")
    void testAccountObjectsInSnapshot() throws Exception {
        // Create snapshot with Account and Warehouse objects included
        SnapshotControl snapshotControl = new SnapshotControl(database, Account.class, Warehouse.class);
        
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, snapshotControl);
        
        // Verify Account objects are present in snapshot
        Set<Account> accounts = snapshot.get(Account.class);
        assertNotNull(accounts, "Account objects should be present in snapshot");
        assertFalse(accounts.isEmpty(), "Should have at least one Account object");
        
        System.out.println("Found " + accounts.size() + " Account objects in snapshot");
        for (Account account : accounts) {
            System.out.println("Account: " + account.getName() + 
                             " (Region: " + account.getRegion() + 
                             ", Cloud: " + account.getCloud() + ")");
        }
    }
    
    @Test
    @DisplayName("Warehouse objects should be contained within Account objects")
    void testWarehousesInAccountContainer() throws Exception {
        SnapshotControl snapshotControl = new SnapshotControl(database, Account.class, Warehouse.class);
        
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, snapshotControl);
        
        // Get Account objects
        Set<Account> accounts = snapshot.get(Account.class);
        assertFalse(accounts.isEmpty(), "Should have Account objects");
        
        // Verify warehouses are contained within accounts
        boolean foundTestWarehouse = false;
        for (Account account : accounts) {
            List<Warehouse> warehouses = account.getDatabaseObjects(Warehouse.class);
            System.out.println("Account " + account.getName() + " contains " + warehouses.size() + " warehouses");
            
            for (Warehouse warehouse : warehouses) {
                System.out.println("  Warehouse: " + warehouse.getName() + 
                                 " (Size: " + warehouse.getSize() + 
                                 ", State: " + warehouse.getState() + 
                                 ", Comment: " + warehouse.getComment() + ")");
                
                if (testWarehouseName.equals(warehouse.getName())) {
                    foundTestWarehouse = true;
                    
                    // Validate test warehouse properties
                    assertEquals("MEDIUM", warehouse.getSize(), "Warehouse size should be MEDIUM");
                    assertEquals(300, warehouse.getAutoSuspend(), "Auto suspend should be 300");
                    assertEquals(Boolean.TRUE, warehouse.getAutoResume(), "Auto resume should be TRUE");
                    assertTrue(warehouse.getComment() != null && 
                             warehouse.getComment().contains("Test warehouse for Account integration testing"),
                             "Comment should contain expected text");
                }
            }
        }
        
        assertTrue(foundTestWarehouse, "Should find the test warehouse within Account container");
    }
    
    @Test
    @DisplayName("Warehouse objects should have comprehensive attribute coverage")
    void testWarehouseAttributeCoverage() throws Exception {
        SnapshotControl snapshotControl = new SnapshotControl(database, Account.class, Warehouse.class);
        
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, snapshotControl);
        
        // Find test warehouse
        Warehouse testWarehouse = null;
        for (Account account : snapshot.get(Account.class)) {
            for (Warehouse warehouse : account.getDatabaseObjects(Warehouse.class)) {
                if (testWarehouseName.equals(warehouse.getName())) {
                    testWarehouse = warehouse;
                    break;
                }
            }
            if (testWarehouse != null) break;
        }
        
        assertNotNull(testWarehouse, "Should find test warehouse");
        
        // Validate essential attributes that should always be set
        assertNotNull(testWarehouse.getName(), "Name should be set");
        assertNotNull(testWarehouse.getSize(), "Size should be set");
        assertNotNull(testWarehouse.getState(), "State should be set");
        
        // Validate configuration attributes with more lenient checks
        // Auto resume and auto suspend may be null if not explicitly set by Snowflake
        // But we can validate they are correctly typed when present
        if (testWarehouse.getAutoResume() != null) {
            assertTrue(testWarehouse.getAutoResume() instanceof Boolean, 
                "Auto resume should be Boolean type when present");
        } else {
            // If null, validate that it was created with our settings and query Snowflake directly
            System.out.println("⚠️  Auto resume is null, querying Snowflake directly...");
            // Use direct SQL query to validate the actual value
            String sql = "SHOW WAREHOUSES LIKE '" + testWarehouseName + "'";
            try (Statement stmt = ((JdbcConnection) database.getConnection()).createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    String autoResume = rs.getString("auto_resume");
                    System.out.println("Direct query - auto_resume: '" + autoResume + "'");
                    assertNotNull(autoResume, "Direct query should return auto_resume value");
                }
            }
        }
        
        if (testWarehouse.getAutoSuspend() != null) {
            assertTrue(testWarehouse.getAutoSuspend() instanceof Integer,
                "Auto suspend should be Integer type when present");
        } else {
            System.out.println("⚠️  Auto suspend is null, querying Snowflake directly...");
            String sql = "SHOW WAREHOUSES LIKE '" + testWarehouseName + "'";
            try (Statement stmt = ((JdbcConnection) database.getConnection()).createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    int autoSuspend = rs.getInt("auto_suspend");
                    boolean wasNull = rs.wasNull();
                    System.out.println("Direct query - auto_suspend: " + autoSuspend + ", wasNull: " + wasNull);
                    if (!wasNull) {
                        // If Snowflake has a value, our snapshot should capture it
                        fail("Auto suspend should not be null when Snowflake has value: " + autoSuspend);
                    }
                }
            }
        }
        
        // Validate configuration properties (included in diffs)
        assertEquals("MEDIUM", testWarehouse.getSize());
        assertEquals(Integer.valueOf(300), testWarehouse.getAutoSuspend());
        assertEquals(Boolean.TRUE, testWarehouse.getAutoResume());
        
        // Validate state properties (excluded from diffs) are captured
        assertNotNull(testWarehouse.getState(), "State should be captured");
        
        System.out.println("Warehouse attribute validation passed:");
        System.out.println("  Name: " + testWarehouse.getName());
        System.out.println("  Size: " + testWarehouse.getSize());
        System.out.println("  State: " + testWarehouse.getState());
        System.out.println("  Auto Suspend: " + testWarehouse.getAutoSuspend());
        System.out.println("  Auto Resume: " + testWarehouse.getAutoResume());
        System.out.println("  Comment: " + testWarehouse.getComment());
    }
    
    @Test
    @DisplayName("Account and Warehouse objects should be discoverable via snapshot queries")  
    void testSnapshotQueries() throws Exception {
        SnapshotControl snapshotControl = new SnapshotControl(database, Account.class, Warehouse.class);
        
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, snapshotControl);
        
        // Test Account discovery - check if snapshot has Account objects
        Set<Account> foundAccounts = snapshot.get(Account.class);
        assertNotNull(foundAccounts, "Should be able to query for Account objects");
        assertFalse(foundAccounts.isEmpty(), "Should find Account objects via query");
        
        // Test Warehouse discovery  
        Warehouse exampleWarehouse = new Warehouse();
        @SuppressWarnings("unchecked")
        Set<Warehouse> foundWarehouses = (Set<Warehouse>) snapshot.get(exampleWarehouse);
        assertNotNull(foundWarehouses, "Should be able to query for Warehouse objects");
        assertFalse(foundWarehouses.isEmpty(), "Should find Warehouse objects via query");
        
        // Verify test warehouse is discoverable
        Warehouse specificWarehouse = new Warehouse();
        specificWarehouse.setName(testWarehouseName);
        Warehouse foundWarehouse = (Warehouse) snapshot.get(specificWarehouse);
        assertNotNull(foundWarehouse, "Should find specific warehouse by name");
        assertEquals(testWarehouseName, foundWarehouse.getName(), "Found warehouse should match name");
        
        System.out.println("Snapshot query validation passed:");
        System.out.println("  Found " + foundAccounts.size() + " Account objects via query");
        System.out.println("  Found " + foundWarehouses.size() + " Warehouse objects via query"); 
        System.out.println("  Successfully found specific warehouse: " + foundWarehouse.getName());
    }
    
    @Test
    @DisplayName("Account objects should be independent of Catalog hierarchy")
    void testAccountIndependenceFromCatalog() throws Exception {
        SnapshotControl snapshotControl = new SnapshotControl(database, Account.class, Warehouse.class);
        
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, snapshotControl);
        
        Set<Account> accounts = snapshot.get(Account.class);
        assertFalse(accounts.isEmpty(), "Should have Account objects");
        
        for (Account account : accounts) {
            // Verify Account objects are not contained within other objects (peer-level)
            assertNull(account.getSchema(), "Account should not have schema (peer to Catalog)");
            assertEquals(0, account.getContainingObjects().length, "Account should not be contained in other objects");
            
            // Verify Account objects contain warehouse objects
            List<Warehouse> warehouses = account.getDatabaseObjects(Warehouse.class);
            System.out.println("Account " + account.getName() + " independently contains " + warehouses.size() + " warehouses");
            
            // Verify warehouses are properly contained within accounts
            for (Warehouse warehouse : warehouses) {
                assertNotNull(warehouse.getName(), "Warehouse should have name");
                // Warehouse containment is handled by the Account container
            }
        }
        
        System.out.println("Account independence validation passed - Account objects are peers to Catalog");
    }
}