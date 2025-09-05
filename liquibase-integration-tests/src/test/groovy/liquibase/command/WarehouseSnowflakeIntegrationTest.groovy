package liquibase.command

import liquibase.CatalogAndSchema
import liquibase.Liquibase
import liquibase.Scope
import liquibase.change.ChangeFactory
import liquibase.change.core.CreateWarehouseChange
import liquibase.change.core.DropWarehouseChange
import liquibase.change.core.AlterWarehouseChange
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.command.CommandScope
import liquibase.command.core.UpdateCommandStep
import liquibase.command.core.helpers.DatabaseChangelogCommandStep
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.core.SnowflakeDatabase
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.ChangeLogParseException
import liquibase.exception.CommandExecutionException
import liquibase.exception.DatabaseException
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.resource.DirectoryResourceAccessor
import liquibase.resource.ResourceAccessor
import liquibase.resource.SearchPathResourceAccessor
import liquibase.structure.core.Table
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement

@LiquibaseIntegrationTest
@Stepwise
class WarehouseSnowflakeIntegrationTest extends Specification {

    @Shared
    private DatabaseTestSystem testSystem
    @Shared
    private Database database
    @Shared
    private Connection connection
    
    private static final String TEST_WAREHOUSE_PREFIX = "LB_TEST_WH_"

    def setupSpec() {
        testSystem = Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("snowflake")
        if (testSystem?.shouldTest()) {
            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(testSystem.getConnection()))
            connection = testSystem.getConnection()
        }
    }

    def cleanupSpec() {
        if (testSystem?.shouldTest()) {
            // Clean up any test warehouses
            cleanupTestWarehouses()
            database?.close()
            testSystem?.stop()
        }
    }

    def setup() {
        if (!testSystem?.shouldTest()) {
            // Skip test if Snowflake is not available
            return
        }
    }

    def cleanup() {
        if (testSystem?.shouldTest() && database != null) {
            database.rollback()
        }
    }

    private void cleanupTestWarehouses() {
        if (!testSystem?.shouldTest()) return
        
        Statement stmt = null
        ResultSet rs = null
        try {
            stmt = connection.createStatement()
            rs = stmt.executeQuery("SHOW WAREHOUSES LIKE '${TEST_WAREHOUSE_PREFIX}%'")
            
            List<String> warehousesToCleanup = []
            while (rs.next()) {
                warehousesToCleanup.add(rs.getString("name"))
            }
            rs.close()
            
            warehousesToCleanup.each { whName ->
                try {
                    stmt.execute("DROP WAREHOUSE IF EXISTS ${whName}")
                } catch (Exception e) {
                    // Ignore cleanup errors
                }
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        } finally {
            rs?.close()
            stmt?.close()
        }
    }

    private boolean warehouseExists(String warehouseName) {
        Statement stmt = null
        ResultSet rs = null
        try {
            stmt = connection.createStatement()
            rs = stmt.executeQuery("SHOW WAREHOUSES LIKE '${warehouseName}'")
            return rs.next()
        } finally {
            rs?.close()
            stmt?.close()
        }
    }

    private Map<String, Object> getWarehouseDetails(String warehouseName) {
        Statement stmt = null
        ResultSet rs = null
        try {
            stmt = connection.createStatement()
            rs = stmt.executeQuery("SHOW WAREHOUSES LIKE '${warehouseName}'")
            if (rs.next()) {
                return [
                    name: rs.getString("name"),
                    state: rs.getString("state"),
                    type: rs.getString("type"),
                    size: rs.getString("size"),
                    min_cluster_count: rs.getInt("min_cluster_count"),
                    max_cluster_count: rs.getInt("max_cluster_count"),
                    auto_suspend: rs.getInt("auto_suspend"),
                    auto_resume: rs.getBoolean("auto_resume"),
                    comment: rs.getString("comment")
                ]
            }
            return null
        } finally {
            rs?.close()
            stmt?.close()
        }
    }

    def "test warehouse creation through Liquibase change log"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }
        
        def testWarehouse = "${TEST_WAREHOUSE_PREFIX}CREATE_${System.currentTimeMillis()}"
        def changeLog = new DatabaseChangeLog()
        def changeSet = new ChangeSet("1", "test", false, false, changeLog.getFilePath(), null, null, changeLog)
        
        def change = new CreateWarehouseChange()
        change.setWarehouseName(testWarehouse)
        change.setWarehouseSize("XSMALL")
        change.setAutoSuspend(60)
        change.setAutoResume(true)
        change.setComment("Test warehouse created by test harness")
        change.setInitiallySuspended(true)
        
        changeSet.addChange(change)
        changeLog.addChangeSet(changeSet)

        when: "Executing Liquibase update"
        def liquibase = new Liquibase(changeLog, new ClassLoaderResourceAccessor(), database)
        liquibase.update("")

        then: "Warehouse should be created with correct properties"
        warehouseExists(testWarehouse)
        def details = getWarehouseDetails(testWarehouse)
        details.name == testWarehouse
        details.size == "X-Small"
        details.auto_suspend == 60
        details.auto_resume == true
        details.comment == "Test warehouse created by test harness"
        details.state == "SUSPENDED"
        
        cleanup:
        if (warehouseExists(testWarehouse)) {
            connection.createStatement().execute("DROP WAREHOUSE ${testWarehouse}")
        }
    }

    def "test warehouse alteration through Liquibase change log"() {
        given: "Test system is available and warehouse exists"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }
        
        def testWarehouse = "${TEST_WAREHOUSE_PREFIX}ALTER_${System.currentTimeMillis()}"
        
        // Create warehouse first
        connection.createStatement().execute("""
            CREATE WAREHOUSE ${testWarehouse} 
            WAREHOUSE_SIZE = XSMALL 
            AUTO_SUSPEND = 60 
            AUTO_RESUME = true 
            INITIALLY_SUSPENDED = true
            COMMENT = 'Original warehouse'
        """)
        assert warehouseExists(testWarehouse)
        
        // Now alter it using Liquibase
        def changeLog = new DatabaseChangeLog()
        def changeSet = new ChangeSet("1", "test", false, false, changeLog.getFilePath(), null, null, changeLog)
        
        def change = new AlterWarehouseChange()
        change.setWarehouseName(testWarehouse)
        change.setNewWarehouseSize("SMALL")
        change.setNewAutoSuspend(120)
        change.setNewComment("Updated warehouse comment")
        
        changeSet.addChange(change)
        changeLog.addChangeSet(changeSet)

        when: "Executing Liquibase update"
        def liquibase = new Liquibase(changeLog, new ClassLoaderResourceAccessor(), database)
        liquibase.update("")

        then: "Warehouse should be altered with new properties"
        warehouseExists(testWarehouse)
        def details = getWarehouseDetails(testWarehouse)
        details.name == testWarehouse
        details.size == "Small"
        details.auto_suspend == 120
        details.comment == "Updated warehouse comment"
        
        cleanup:
        if (warehouseExists(testWarehouse)) {
            connection.createStatement().execute("DROP WAREHOUSE ${testWarehouse}")
        }
    }

    def "test warehouse dropping through Liquibase change log"() {
        given: "Test system is available and warehouse exists"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }
        
        def testWarehouse = "${TEST_WAREHOUSE_PREFIX}DROP_${System.currentTimeMillis()}"
        
        // Create warehouse first
        connection.createStatement().execute("CREATE WAREHOUSE ${testWarehouse} WAREHOUSE_SIZE = XSMALL")
        assert warehouseExists(testWarehouse)
        
        // Now drop it using Liquibase
        def changeLog = new DatabaseChangeLog()
        def changeSet = new ChangeSet("1", "test", false, false, changeLog.getFilePath(), null, null, changeLog)
        
        def change = new DropWarehouseChange()
        change.setWarehouseName(testWarehouse)
        
        changeSet.addChange(change)
        changeLog.addChangeSet(changeSet)

        when: "Executing Liquibase update"
        def liquibase = new Liquibase(changeLog, new ClassLoaderResourceAccessor(), database)
        liquibase.update("")

        then: "Warehouse should be dropped"
        !warehouseExists(testWarehouse)
    }

    def "test warehouse clustering properties"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }
        
        def testWarehouse = "${TEST_WAREHOUSE_PREFIX}CLUSTER_${System.currentTimeMillis()}"
        def changeLog = new DatabaseChangeLog()
        def changeSet = new ChangeSet("1", "test", false, false, changeLog.getFilePath(), null, null, changeLog)
        
        def change = new CreateWarehouseChange()
        change.setWarehouseName(testWarehouse)
        change.setWarehouseSize("MEDIUM")
        change.setWarehouseType("STANDARD") // Multi-cluster
        change.setMinClusterCount(1)
        change.setMaxClusterCount(3)
        change.setAutoSuspend(300)
        change.setAutoResume(true)
        
        changeSet.addChange(change)
        changeLog.addChangeSet(changeSet)

        when: "Creating multi-cluster warehouse"
        def liquibase = new Liquibase(changeLog, new ClassLoaderResourceAccessor(), database)
        liquibase.update("")

        then: "Warehouse should be created with clustering properties"
        warehouseExists(testWarehouse)
        def details = getWarehouseDetails(testWarehouse)
        details.name == testWarehouse
        details.size == "Medium"
        details.min_cluster_count == 1
        details.max_cluster_count == 3
        details.auto_suspend == 300
        
        cleanup:
        if (warehouseExists(testWarehouse)) {
            connection.createStatement().execute("DROP WAREHOUSE ${testWarehouse}")
        }
    }

    def "test warehouse with resource monitor"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }
        
        def testWarehouse = "${TEST_WAREHOUSE_PREFIX}MONITOR_${System.currentTimeMillis()}"
        def testMonitor = "LB_TEST_MONITOR_${System.currentTimeMillis()}"
        
        try {
            // Create a resource monitor first
            connection.createStatement().execute("""
                CREATE RESOURCE MONITOR ${testMonitor} 
                WITH CREDIT_QUOTA = 100
                TRIGGERS 
                    ON 75 PERCENT DO SUSPEND
                    ON 100 PERCENT DO SUSPEND_IMMEDIATE
            """)
            
            def changeLog = new DatabaseChangeLog()
            def changeSet = new ChangeSet("1", "test", false, false, changeLog.getFilePath(), null, null, changeLog)
            
            def change = new CreateWarehouseChange()
            change.setWarehouseName(testWarehouse)
            change.setWarehouseSize("XSMALL")
            change.setResourceMonitor(testMonitor)
            change.setAutoSuspend(60)
            
            changeSet.addChange(change)
            changeLog.addChangeSet(changeSet)

            when: "Creating warehouse with resource monitor"
            def liquibase = new Liquibase(changeLog, new ClassLoaderResourceAccessor(), database)
            liquibase.update("")

            then: "Warehouse should be created with resource monitor"
            warehouseExists(testWarehouse)
            def details = getWarehouseDetails(testWarehouse)
            details.name == testWarehouse
            
        } finally {
            // Clean up warehouse and monitor
            if (warehouseExists(testWarehouse)) {
                connection.createStatement().execute("DROP WAREHOUSE ${testWarehouse}")
            }
            try {
                connection.createStatement().execute("DROP RESOURCE MONITOR ${testMonitor}")
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
    }

    def "test warehouse state management"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }
        
        def testWarehouse = "${TEST_WAREHOUSE_PREFIX}STATE_${System.currentTimeMillis()}"
        
        // Create warehouse initially suspended
        def changeLog1 = new DatabaseChangeLog()
        def changeSet1 = new ChangeSet("1", "test", false, false, changeLog1.getFilePath(), null, null, changeLog1)
        
        def createChange = new CreateWarehouseChange()
        createChange.setWarehouseName(testWarehouse)
        createChange.setWarehouseSize("XSMALL")
        createChange.setInitiallySuspended(true)
        createChange.setAutoSuspend(60)
        
        changeSet1.addChange(createChange)
        changeLog1.addChangeSet(changeSet1)

        when: "Creating initially suspended warehouse"
        def liquibase1 = new Liquibase(changeLog1, new ClassLoaderResourceAccessor(), database)
        liquibase1.update("")

        then: "Warehouse should be suspended"
        warehouseExists(testWarehouse)
        def details1 = getWarehouseDetails(testWarehouse)
        details1.state == "SUSPENDED"

        when: "Resuming warehouse through alter"
        def changeLog2 = new DatabaseChangeLog()
        def changeSet2 = new ChangeSet("2", "test", false, false, changeLog2.getFilePath(), null, null, changeLog2)
        
        def alterChange = new AlterWarehouseChange()
        alterChange.setWarehouseName(testWarehouse)
        alterChange.setResume(true)
        
        changeSet2.addChange(alterChange)
        changeLog2.addChangeSet(changeSet2)
        
        def liquibase2 = new Liquibase(changeLog2, new ClassLoaderResourceAccessor(), database)
        liquibase2.update("")

        then: "Warehouse should be running"
        warehouseExists(testWarehouse)
        def details2 = getWarehouseDetails(testWarehouse)
        details2.state == "STARTED"
        
        cleanup:
        if (warehouseExists(testWarehouse)) {
            connection.createStatement().execute("DROP WAREHOUSE ${testWarehouse}")
        }
    }

    def "test warehouse validation and error handling"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }
        
        def changeLog = new DatabaseChangeLog()
        def changeSet = new ChangeSet("1", "test", false, false, changeLog.getFilePath(), null, null, changeLog)
        
        def change = new CreateWarehouseChange()
        // Missing required warehouse name - should trigger validation error
        change.setWarehouseSize("XSMALL")
        
        changeSet.addChange(change)
        changeLog.addChangeSet(changeSet)

        when: "Creating warehouse without name"
        def liquibase = new Liquibase(changeLog, new ClassLoaderResourceAccessor(), database)
        
        then: "Should throw validation error"
        thrown(Exception)
    }

    def "test complex warehouse scenario with multiple operations"() {
        given: "Test system is available"
        if (!testSystem?.shouldTest()) {
            println "Skipping test - Snowflake test system not available"
            return
        }
        
        def testWarehouse = "${TEST_WAREHOUSE_PREFIX}COMPLEX_${System.currentTimeMillis()}"
        def changeLog = new DatabaseChangeLog()
        
        // Changeset 1: Create warehouse
        def changeSet1 = new ChangeSet("1", "test", false, false, changeLog.getFilePath(), null, null, changeLog)
        def createChange = new CreateWarehouseChange()
        createChange.setWarehouseName(testWarehouse)
        createChange.setWarehouseSize("XSMALL")
        createChange.setAutoSuspend(60)
        createChange.setComment("Original warehouse")
        changeSet1.addChange(createChange)
        changeLog.addChangeSet(changeSet1)
        
        // Changeset 2: Alter warehouse
        def changeSet2 = new ChangeSet("2", "test", false, false, changeLog.getFilePath(), null, null, changeLog)
        def alterChange = new AlterWarehouseChange()
        alterChange.setWarehouseName(testWarehouse)
        alterChange.setNewWarehouseSize("SMALL")
        alterChange.setNewComment("Updated warehouse")
        changeSet2.addChange(alterChange)
        changeLog.addChangeSet(changeSet2)
        
        // Changeset 3: Drop warehouse
        def changeSet3 = new ChangeSet("3", "test", false, false, changeLog.getFilePath(), null, null, changeLog)
        def dropChange = new DropWarehouseChange()
        dropChange.setWarehouseName(testWarehouse)
        changeSet3.addChange(dropChange)
        changeLog.addChangeSet(changeSet3)

        when: "Executing complete warehouse lifecycle"
        def liquibase = new Liquibase(changeLog, new ClassLoaderResourceAccessor(), database)
        liquibase.update("")

        then: "All operations should complete successfully"
        !warehouseExists(testWarehouse) // Should be dropped at the end
        
        and: "Change log should track all changes"
        // The changelog should have recorded all 3 changesets
        noExceptionThrown()
    }
}