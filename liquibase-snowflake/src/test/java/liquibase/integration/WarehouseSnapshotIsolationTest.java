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
import liquibase.snapshot.jvm.WarehouseSnapshotGeneratorSnowflake;
import liquibase.util.TestDatabaseConfigUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.sql.Connection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Isolated test to verify WarehouseSnapshotGenerator works correctly.
 */
@DisplayName("Warehouse Snapshot Isolation Test")
public class WarehouseSnapshotIsolationTest {
    
    private Database database;
    private Connection rawConnection;
    
    @BeforeEach
    void setUp() throws Exception {
        rawConnection = TestDatabaseConfigUtil.getSnowflakeConnection();
        JdbcConnection jdbcConnection = new JdbcConnection(rawConnection);
        database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConnection);
        
        assertTrue(database instanceof SnowflakeDatabase, "Must be Snowflake database");
    }
    
    @Test
    @DisplayName("Direct warehouse snapshot test")
    public void testWarehouseSnapshotDirectly() throws Exception {
        
        // Test with Account and Warehouse classes
        SnapshotControl snapshotControl = new SnapshotControl(database, Account.class, Warehouse.class);
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, snapshotControl);
            
        
        // Check what warehouses are found
        Set<Warehouse> warehouses = snapshot.get(Warehouse.class);
        
        if (warehouses != null) {
            for (Warehouse wh : warehouses) {
            }
        }
        
        // Check accounts
        Set<Account> accounts = snapshot.get(Account.class);
        
        if (accounts != null) {
            for (Account account : accounts) {
                // Note: getWarehouses() method doesn't exist - warehouses are added via addDatabaseObject()
            }
        }
        
    }
    
    @Test
    @DisplayName("WarehouseSnapshotGenerator instantiation test")  
    public void testWarehouseGeneratorInstantiation() {
        
        WarehouseSnapshotGeneratorSnowflake generator = new WarehouseSnapshotGeneratorSnowflake();
        
        // Test priority
        int priority = generator.getPriority(Warehouse.class, database);
        assertEquals(5, priority, "Should return PRIORITY_DATABASE for Warehouse on Snowflake");
        
        int accountPriority = generator.getPriority(Account.class, database);  
        assertEquals(50, accountPriority, "Should return PRIORITY_ADDITIONAL for Account (addsTo)");
        
        // Test addsTo
        Class<?>[] addsTo = generator.addsTo();
        assertNotNull(addsTo, "addsTo should not be null");
        assertEquals(1, addsTo.length, "Should addsTo 1 class");
        assertEquals(Account.class, addsTo[0], "Should addsTo Account.class");
        
    }
}