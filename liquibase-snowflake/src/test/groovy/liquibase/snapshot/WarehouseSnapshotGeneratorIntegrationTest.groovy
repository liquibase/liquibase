package liquibase.snapshot

import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.database.object.Warehouse
import liquibase.snapshot.DatabaseSnapshot
import liquibase.snapshot.SnapshotControl
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.structure.core.Catalog
import liquibase.structure.core.Schema
import liquibase.util.TestDatabaseConfigUtil
import spock.lang.Specification
import java.sql.Connection
import java.sql.Statement
import java.sql.ResultSet

/**
 * Integration tests for WarehouseSnapshotGenerator using real Snowflake database.
 * Updated to use modern TestDatabaseConfigUtil with YAML configuration.
 */
class WarehouseSnapshotGeneratorIntegrationTest extends Specification {

    Database database
    Connection connection
    List<String> createdTestObjects = []

    def setup() {
        connection = TestDatabaseConfigUtil.getSnowflakeConnection()
        database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection))
    }
    
    def cleanup() {
        // Clean up all created test warehouses
        createdTestObjects.each { warehouseName ->
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP WAREHOUSE IF EXISTS ${warehouseName}")
                println("Cleaned up test warehouse: ${warehouseName}")
            } catch (Exception e) {
                System.err.println("Failed to cleanup test warehouse ${warehouseName}: ${e.getMessage()}")
            }
        }
        
        if (connection && !connection.isClosed()) {
            connection.close()
        }
    }

    def "warehouse snapshot should capture all properties from existing warehouse"() {
        given: "A test warehouse with comprehensive properties"
        String testWarehouseName = "SNAPSHOT_INTEGRATION_TEST_${System.currentTimeMillis()}"
        createdTestObjects.add(testWarehouseName)
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE WAREHOUSE ${testWarehouseName}
                WITH WAREHOUSE_SIZE = 'SMALL'
                     AUTO_SUSPEND = 300
                     AUTO_RESUME = TRUE
                     MIN_CLUSTER_COUNT = 1
                     MAX_CLUSTER_COUNT = 2
                     SCALING_POLICY = 'STANDARD'
                     INITIALLY_SUSPENDED = FALSE
                     COMMENT = 'Integration test warehouse'
            """)
        }
        
        when: "Using direct Warehouse snapshot request"
        Catalog catalog = new Catalog(database.getDefaultCatalogName())
        Warehouse exampleWarehouse = new Warehouse()
        exampleWarehouse.setName(testWarehouseName)
        
        SnapshotControl snapshotControl = new SnapshotControl(database, Warehouse.class)
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot([exampleWarehouse] as liquibase.structure.DatabaseObject[], 
                          database, snapshotControl)
        
        then: "The warehouse should be captured with all properties"
        Warehouse snapshotted = snapshot.get(exampleWarehouse)
        snapshotted != null
        snapshotted.getName() == testWarehouseName
        snapshotted.getSize() == "SMALL"
        snapshotted.getAutoSuspend() == 300
        snapshotted.getAutoResume() == true
        snapshotted.getMinClusterCount() == 1
        snapshotted.getMaxClusterCount() == 2
        snapshotted.getScalingPolicy() == "STANDARD"
        snapshotted.getComment() == "Integration test warehouse"
    }

    def "warehouse snapshot should return null for non-existent warehouse"() {
        given: "A non-existent warehouse name"
        String nonExistentWarehouse = "NON_EXISTENT_${System.currentTimeMillis()}"
        
        when: "Attempting to snapshot the non-existent warehouse"
        Warehouse exampleWarehouse = new Warehouse()
        exampleWarehouse.setName(nonExistentWarehouse)
        
        SnapshotControl snapshotControl = new SnapshotControl(database, Warehouse.class)
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance()
            .createSnapshot([exampleWarehouse] as liquibase.structure.DatabaseObject[], 
                          database, snapshotControl)
        
        then: "Should return null for non-existent warehouse"
        snapshot.get(exampleWarehouse) == null
    }

    def "warehouse should validate null name and throw exception"() {
        when: "Attempting to set null name on warehouse"
        Warehouse warehouse = new Warehouse()
        warehouse.setName(null)
        
        then: "Should throw IllegalArgumentException"
        thrown(IllegalArgumentException)
    }
    
    def "warehouse should validate empty name and throw exception"() {
        when: "Attempting to set empty name on warehouse"
        Warehouse warehouse = new Warehouse()
        warehouse.setName("")
        
        then: "Should throw IllegalArgumentException"
        thrown(IllegalArgumentException)
    }

    def "can verify warehouse exists via SHOW WAREHOUSES"() {
        given: "A test warehouse"
        String testWarehouseName = "SHOW_VERIFICATION_TEST_${System.currentTimeMillis()}"
        createdTestObjects.add(testWarehouseName)
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE WAREHOUSE ${testWarehouseName} WITH WAREHOUSE_SIZE = 'XSMALL'")
        }
        
        when: "Checking warehouse existence via SHOW WAREHOUSES"
        boolean exists = false
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW WAREHOUSES LIKE '${testWarehouseName}'")) {
            exists = rs.next()
        }
        
        then: "The warehouse should be found"
        exists
    }
}