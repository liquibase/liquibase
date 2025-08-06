package liquibase.debug;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;

import java.sql.Connection;
import java.util.Set;

/**
 * Debug test to understand snapshot behavior with Account → Warehouse architecture.
 */
@DisplayName("Snapshot Debug Tests")
public class SnapshotDebugTest {
    
    @Test
    @DisplayName("Debug: Investigate snapshot contents")
    void debugSnapshotContents() throws Exception {
        Connection rawConnection;
        Database database;
        
        try {
            rawConnection = TestDatabaseConfigUtil.getSnowflakeConnection();
            JdbcConnection jdbcConnection = new JdbcConnection(rawConnection);
            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConnection);
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "Cannot connect to Snowflake: " + e.getMessage());
            return;
        }
        
        // Create snapshot with Account and Warehouse objects
        SnapshotControl snapshotControl = new SnapshotControl(database, Account.class, Warehouse.class);
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, snapshotControl);
        
        System.out.println("=== SNAPSHOT DEBUG INFO ===");
        
        // Check Account objects
        Set<Account> accounts = snapshot.get(Account.class);
        System.out.println("Found " + accounts.size() + " Account objects");
        
        for (Account account : accounts) {
            System.out.println("Account: " + account.getName() + 
                             " (Region: " + account.getRegion() + 
                             ", Cloud: " + account.getCloud() + ")");
            
            // Check warehouses within this account
            if (account.getDatabaseObjects() != null) {
                java.util.List<Warehouse> warehouses = account.getDatabaseObjects(Warehouse.class);
                System.out.println("  Contains " + warehouses.size() + " warehouses:");
                
                for (Warehouse warehouse : warehouses) {
                    System.out.println("    Warehouse: " + warehouse.getName() + 
                                     " (Size: " + warehouse.getSize() + 
                                     ", State: " + warehouse.getState() + ")");
                }
            } else {
                System.out.println("  Account.getDatabaseObjects() returned null");
            }
        }
        
        // Also check direct Warehouse objects in snapshot
        Set<Warehouse> directWarehouses = snapshot.get(Warehouse.class);
        System.out.println("Found " + directWarehouses.size() + " direct Warehouse objects in snapshot");
        
        for (Warehouse warehouse : directWarehouses) {
            System.out.println("Direct Warehouse: " + warehouse.getName() + 
                             " (Size: " + warehouse.getSize() + 
                             ", State: " + warehouse.getState() + ")");
        }
        
        System.out.println("=== END SNAPSHOT DEBUG ===");
        
        rawConnection.close();
    }
}